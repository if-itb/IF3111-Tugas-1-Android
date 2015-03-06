package com.azaky.android.pbd.tomandjerry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {
    private static final String API_ENDPOINT = "http://167.205.32.46/pbd/api";
    private static final String API_TRACK_PARAMETER = "/track?nim=13512076";
    private static final String API_CATCH_PARAMETER = "/catch";
    private static final String NIM = "13512076";

    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final int CODE_SCAN = 0;

    private static final String STATE_EPOCH = "epoch";
    private static final String STATE_LATITUDE = "latitude";
    private static final String STATE_LONGITUDE = "longitude";
    private static final int CONNECTION_TIMEOUT = 5000;

    private TextView txtTimer;
    private Button btnCatch;
    private ImageView imgCompass;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private CompassHelper mCompassHelper;

//    private JSONObject trackInfo;
    private CountDownTimer timer;
    private long epoch = -1;
    private LatLng jerryLocation;
    private Marker jerryMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get objects
        txtTimer = (TextView) findViewById(R.id.txtTimeLeft);
        btnCatch = (Button) findViewById(R.id.btnCatch);
        imgCompass = (ImageView) findViewById(R.id.imgCompass);

        // set up map
        setUpMapIfNeeded();
//        buildGoogleApiClient();

        // initialize Jerry
        if (savedInstanceState != null) {
            // load data from savedInstanceState
            epoch = savedInstanceState.getLong(STATE_EPOCH);
            if (epoch != -1) {
                jerryLocation = new LatLng(savedInstanceState.getDouble(STATE_LATITUDE),
                        savedInstanceState.getDouble(STATE_LONGITUDE));
                initializeJerry();
            }
        } else {
            // load data from API
            getTrackPosition("Loading data from Spike ...");
        }

        // initialize compass
        mCompassHelper = new CompassHelper(this, imgCompass);
        mCompassHelper.registerListener();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the state
        savedInstanceState.putLong(STATE_EPOCH, epoch);

        if (epoch != -1 && jerryLocation != null) {
            savedInstanceState.putDouble(STATE_LATITUDE, jerryLocation.latitude);
            savedInstanceState.putDouble(STATE_LONGITUDE, jerryLocation.longitude);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the state
        epoch = savedInstanceState.getLong(STATE_EPOCH);
        if (epoch != -1) {
            jerryLocation = new LatLng(savedInstanceState.getDouble(STATE_LATITUDE),
                    savedInstanceState.getDouble(STATE_LONGITUDE));
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mCompassHelper.unregisterListener();

        // stop the timer
        if (timer != null) {
            timer.cancel();
        }

        // save the epoch and location information
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // continue the compass
        mCompassHelper.registerListener();

        // continue the thing
        if (epoch != -1) {
            initializeTimer();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }

    public void onButtonCatchClick(View view) {
        scanCode();
    }

    public void onButtonRefreshClick(View view) {
        getTrackPosition("Refreshing data ...");
    }

    public void scanCode() {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, CODE_SCAN);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No Scanner Found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CODE_SCAN) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String token = intent.getStringExtra("SCAN_RESULT");
                catchJerry(token);
            }
        }
    }

    /**
     * Initializing timer. Variable epoch must be defined first
     */
    public void initializeTimer() {
        timer = new CountDownTimer(epoch - System.currentTimeMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long second = (millisUntilFinished / 1000) % 60;
                long minute = (millisUntilFinished / (60 * 1000)) % 60;
                long hour = (millisUntilFinished / (60 * 60 * 1000));
                String formatted = String.format("%d:%02d:%02d", hour, minute, second);
                txtTimer.setText("Time Left: " + formatted);
            }

            @Override
            public void onFinish() {
                txtTimer.setText("Time's up!");
                getTrackPosition("Reloading data from Spike ..."); // is it even allowed?
            }
        };
        timer.start();
    }

    /**
     * Once we have the position of Jerry and the time limit, initialize the map and the timer.
     * trackInfo should be initialized first
     */
    public void initializeJerry() {
        // update maps
        if (jerryMarker != null) {
            jerryMarker.remove();
        }
        jerryMarker = mMap.addMarker(new MarkerOptions().position(jerryLocation).title("Jerry"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerryLocation, 17.0f));

        // update timer
        initializeTimer();
    }

    public void getTrackPosition(String message) {
        // loading dialog
        final ProgressDialog dialog = ProgressDialog.show(this, "", message, true);
        final Context thisActivity = this;
        final DialogInterface.OnClickListener onRetry = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getTrackPosition("Retrying ...");
            }
        };

        // getting track position from API through AsyncTask
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    // make a http get request
                    HttpGet request = new HttpGet();
                    URI website = new URI(API_ENDPOINT + API_TRACK_PARAMETER);
                    request.setURI(website);

                    // set timeout
                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
                    HttpConnectionParams.setSoTimeout(httpParameters, CONNECTION_TIMEOUT);
                    HttpClient httpClient = new DefaultHttpClient(httpParameters);

                    // execute the request
                    HttpResponse response = httpClient.execute(request);

                    // show retry dialog if the connection was not successful
                    if (response.getStatusLine().getStatusCode() != 200) {
                        new RetryDialog("Failed to Retrieve Data: Error "
                                + response.getStatusLine().getStatusCode(), onRetry
                                ).show(getFragmentManager(), "getTrackPosition");
                        return null;
                    } else {
                        // get the result
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));
                        String line;
                        StringBuilder resultString = new StringBuilder();
                        while ((line = in.readLine()) != null) {
                            resultString.append(line);
                        }

                        return resultString.toString();
                    }
                } catch (IOException e) {
                    // log this error
                    Log.e("getTrackPosition", "IOException: " + e.getMessage());

                    // show retry dialog
                    new RetryDialog("Failed to Retrieve Data: " + e.getMessage(), onRetry
                            ).show(getFragmentManager(), "getTrackPosition");

                    return null;
                } catch (URISyntaxException e) {
                    Log.e("getTrackPosition", "URISyntax: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                dialog.dismiss();
                if (result == null) return;

                try {
                    // parse result to JSON Object
                    JSONObject trackInfo = new JSONObject(result);
                    epoch = trackInfo.getLong("valid_until") * 1000;
                    jerryLocation = new LatLng(trackInfo.getDouble("lat"), trackInfo.getDouble("long"));

                    // let this be handled by initializeJerry
                    initializeJerry();

                } catch (JSONException e) {
                    Log.e("getTrackPosition", "JSONException: " + e.getMessage());

                    // sets epoch back to -1
                    epoch = -1;
                }
            }
        }.execute();
    }

    public void catchJerry(final String token) {
        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Getting response from Spike...", true);
        final Context thisActivity = this;

        new AsyncTask<String, String, String>() {
            HttpResponse response = null;

            @Override
            protected String doInBackground(String... args) {
                try {
                    // make a http connection
                    HttpPost request = new HttpPost();
                    URI website = new URI(API_ENDPOINT + API_CATCH_PARAMETER);
                    request.setURI(website);

                    // set parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("token", token));
                    params.add(new BasicNameValuePair("nim", NIM));
                    request.setEntity(new UrlEncodedFormEntity(params));

                    // set timeout
                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
                    HttpConnectionParams.setSoTimeout(httpParameters, CONNECTION_TIMEOUT);
                    HttpClient httpClient = new DefaultHttpClient(httpParameters);

                    // execute the request
                    response = httpClient.execute(request);

                    return null;
                } catch (IOException e) {
                    Log.e("catchJerry", "IOException: " + e.getMessage());

                    // show retry dialog
                    new RetryDialog("Failed to Retrieve Data: " + e.getMessage(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    catchJerry(token);
                                }
                            }).show(getFragmentManager(), "catchJery");

                    response = null;
                } catch (URISyntaxException e) {
                    Log.e("catchJerry", "URISyntaxException: " + e.getMessage());

                    response = null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String res) {
                // dismiss the loading dialog
                dialog.dismiss();

                // if the response is null, then there is something wrong
                if (response == null) return;

                // the dialog we will use to display the message/feedback
                ResultDialog resultDialog = null;

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    // the connection was not successful
                    resultDialog = new ResultDialog(statusCode,
                            response.getStatusLine().getReasonPhrase());
                } else {
                    StringBuilder resultString = new StringBuilder();
                    try {
                        // parsing the retrieved content
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            resultString.append(line);
                        }

                        // getting the values from the JSONObject
                        JSONObject result = new JSONObject(resultString.toString());
                        if (result.has("code")) {
                            int code = result.getInt("code");
                            String message = result.getString("message");
                            resultDialog = new ResultDialog(code, message);
                        }
                    } catch (IOException e) {
                        resultDialog = new ResultDialog(-1, "IOException: " + e.getMessage());
                    } catch (JSONException e) {
                        // handling weird case 200:OK which has form Success: NIM -> token
                        if (resultString.toString().substring(1, 8)
                                .compareToIgnoreCase("success") == 0) {
                            resultDialog = new ResultDialog(200, "OK");
                        }
                        // otherwise it is indeed an error
                        else {
                            resultDialog = new ResultDialog(-1, resultString.toString() + "\n"
                                    + "JSONException: " + e.getMessage());
                        }
                    }
                }

                // show the result dialog
                if (resultDialog != null) {
                    resultDialog.show(getFragmentManager(), "ResultDialog");
                }
            }
        }.execute();
    }

    // dialogs to retry a particular action
    public class RetryDialog extends DialogFragment {
        String message;
        DialogInterface.OnClickListener onOkClickListener;

        public RetryDialog(String message, DialogInterface.OnClickListener onOkClickListener) {
            this.message = message;
            // sets an empty on click listener if the given one is null
            if (onOkClickListener == null) {
                this.onOkClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                };
            } else {
                this.onOkClickListener = onOkClickListener;
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(message)
                    .setPositiveButton("Retry", onOkClickListener)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    // dialogs to show result after the response was retrieved from Spike
    public class ResultDialog extends DialogFragment {
        private int code;
        private String message;

        public ResultDialog(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String mainMessage = code == 200
                    ? "Congratulations! You did it!"
                    : "Ah, that was close! Try again next time!";
            String detailMessage = Integer.toString(code) + ": " + message;
            builder.setTitle("Result")
                    .setMessage(mainMessage + "\n\n" + detailMessage);
            if (code == 200) {
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
            } else {
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                }).setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scanCode();
                    }
                });
            }
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
