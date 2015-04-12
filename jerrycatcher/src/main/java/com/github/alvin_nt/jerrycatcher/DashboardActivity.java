package com.github.alvin_nt.jerrycatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DashboardActivity extends ActionBarActivity 
        implements 
        SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener
{

    // elements of the Activity
    private CompassView mPointer;
    private TextView mTextDistance;
    private TextView mTextTimer;
    private SupportMapFragment mMap;
    private final DecimalFormat decimalFormat = new DecimalFormat();
    
    // sensor objects
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    
    // stores past events
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    
    // ??
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    
    // parameters for rotation
    private float[] mR = new float[9]; // rotation matrix
    private float[] mOrientation = new float[3];
    
    // jerry's params
    private boolean connected;
    private double latJerry, longJerry;
    private long tJerryExpire;
    private Location jerryLocation;
    private Marker jerryMarker;
    private CountDownTimer expiryTimer;
    
    // map parameters
    private GoogleApiClient mGoogleApiClient;
    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(16)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    // QR Code scanning param
    static final String SCAN_APP = "la.droid.qr.scan";
    static final String SCAN_COMPLETE_RESULT = "la.droid.qr.complete"; // get the content and the context
    static final String SCAN_RESULT = "la.droid.qr.result";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mTextDistance = (TextView) findViewById(R.id.distance);
        mTextTimer = (TextView) findViewById(R.id.timer);
        mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        initPointer();
        decimalFormat.setMaximumFractionDigits(2);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .build();

        GoogleMap googleMap = mMap.getMap();
        googleMap.setMyLocationEnabled(true);
    }

    // handles onResume event
    @Override
    protected void onResume() {
        super.onResume();
        
        mGoogleApiClient.connect();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    // handles onPause event
    @Override
    protected void onPause() {
        super.onPause();
        
        mGoogleApiClient.disconnect();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_about) {
            return showAbout();
        } else if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private boolean showAbout() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        
        // When linking text, force to always use default color.
        // This is a workaround for a pressed color state bug
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
        
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if  (event.sensor == mAccelerometer) { // accelerometer event
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) { // magnetometer event
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            
            float radAzimuth = mOrientation[0];
            float degAzimuth = (float)(Math.toDegrees(radAzimuth) + 360) % 360;

            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(currentLocation != null) {
                GeomagneticField geoField = new GeomagneticField(
                        Double.valueOf(currentLocation.getLatitude()).floatValue(),
                        Double.valueOf(currentLocation.getLongitude()).floatValue(),
                        Double.valueOf(currentLocation.getAltitude()).floatValue(),
                        System.currentTimeMillis()
                );
                degAzimuth += geoField.getDeclination();

                // get the target position
                Location target = new Location("Jerry Position");
                target.setLatitude(latJerry);
                target.setLongitude(longJerry);
                float bearing = currentLocation.bearingTo(target);
                float direction = (degAzimuth - bearing);

                mPointer.rotationUpdate(-direction, true);
            }
            // reset
            mLastMagnetometerSet = false;
            mLastAccelerometerSet = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // basically do nothing
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                LOCATION_REQUEST,
                this
        );
        
        refresh();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // do nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        // calculate distance from current position
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                latJerry, longJerry, results);
        
        mTextDistance.setText(getResources().getString(R.string.text_status_distance, decimalFormat.format(results[0])));
        
        // zoom out
        GoogleMap googleMap = mMap.getMap();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(), location.getLongitude()
                )));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14.8f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        refresh();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // display connection error
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String token = intent.getStringExtra(SCAN_RESULT);

                // Show some status dialog
                try {
                    String retStatus = new HttpAsyncTask().execute(token).get();
                    int status = Integer.parseInt(retStatus);

                    if(status == HttpStatus.SC_OK) {
                        Toast toast = Toast.makeText(this, "Success! Jerry is caught.", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast.makeText(this, "Whoops! Jerry escaped. Status code: " + status, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast toast = Toast.makeText(this, "Whoops! An exception occurred: " + e.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        }
    }

    private boolean checkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initPointer() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (CompassView) findViewById(R.id.pointer);
    }

    private void refresh() {
        refreshMap();

        long timeLeft = tJerryExpire * 1000 - System.currentTimeMillis();
        expiryTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds %= 60;

                mTextTimer.setText(String.format("%02d:%02d", minutes, seconds));
                if (minutes >= 3) {
                    mTextTimer.setBackgroundColor(Color.BLUE);
                    mTextTimer.setTextColor(Color.WHITE);
                } else if (minutes >= 1 && minutes < 3) {
                    mTextTimer.setBackgroundColor(Color.YELLOW);
                    mTextTimer.setTextColor(Color.BLACK);
                } else {
                    mTextTimer.setBackgroundColor(Color.RED);
                    mTextTimer.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onFinish() {
                mTextTimer.setText(String.format("%02d:%02d", 0, 0));

                Toast.makeText(DashboardActivity.this, "Time's up! Refreshing...", Toast.LENGTH_SHORT).show();
                refresh();
            }
        }.start();
    }
    
    private void refreshMap() {
        JSONObject json;

        try {
            json = new JSONObject(new HttpAsyncTask().execute().get());

            Resources res = getResources();

            latJerry = json.getDouble(res.getString(R.string.api_jsonGetPosition_attrLat));
            longJerry = json.getDouble(res.getString(R.string.api_jsonGetPosition_attrLong));
            tJerryExpire = json.getLong(res.getString(R.string.api_jsonGetPosition_attrValidUntil));

            LatLng posJerry = new LatLng(latJerry, longJerry);

            // update the map's camera
            GoogleMap map = mMap.getMap();
            CameraUpdate centerJerryLocation = CameraUpdateFactory.newLatLngZoom(
                    posJerry, // lat long
                    15f // zoom param
            );
            map.animateCamera(centerJerryLocation);

            // remove previous marker
            if(jerryMarker != null) {
                jerryMarker.remove();
            }

            // get the time
            Date expiryTime = new Date(tJerryExpire * 1000);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

            // add the marker
            jerryMarker = map.addMarker(new MarkerOptions()
                            .position(posJerry)
                            .title("Valid until " + formatter.format(expiryTime))
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    // used for download dialog
    private static AlertDialog showDownloadDialog(final Activity act, CharSequence title, 
                                          CharSequence message, CharSequence buttonYes,
                                          CharSequence buttonNo)
    {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + SCAN_APP);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    // should not happen
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }
    
    public void scanQr(View v) {
        try {
            Intent intent = new Intent(SCAN_APP);
            intent.putExtra(SCAN_COMPLETE_RESULT, false);
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            // ask the user to download the app
            // TODO: move the strings to the resources
            showDownloadDialog(this, 
                    "Download QR Code app", // title
                    "This app requires QR Code by DroidLa. Do you want to download?", // message
                    "Yes", // yes button
                    "No"); // no button
        }
        
    }
    
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        public final String API_BASEURL = getResources().getString(R.string.api_baseUrl);
        public final String API_GET_POSITION = getResources().getString(R.string.api_getPosition);
        public final String API_NIM = getResources().getString(R.string.api_nim);
        public final String API_POST_TOKEN = getResources().getString(R.string.api_postToken);
        
        @Override
        protected String doInBackground(String... params) {
            String ret;
            
            if(params.length == 0) {
                ret = getPosition();
            } else if(params.length == 1) {
                ret = Integer.valueOf(postToken(params[0])).toString();
            } else {
                ret = null;
            }
            
            return ret;
        }

        /**
         * Gets the position of Jerry from the server *
         * @return JSON response from server
         */
        private String getPosition() {
            String url = String.format(API_BASEURL + API_GET_POSITION, API_NIM);
            
            HttpClient client = new DefaultHttpClient();
            InputStream inputStream;
            StringBuilder result = new StringBuilder();

            try {
                HttpResponse response = client.execute(new HttpGet(url));
                inputStream = response.getEntity().getContent();

                if(inputStream != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }

                    inputStream.close();
                } else {
                    result.append("No result");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /**
         * Uploads the token to the server
         * @param token the token code, to be uploaded to the server
         * @return 0 if successful, 1 if the token is incorrect,
         *          2 if (at least) one of the parameters is invalid, -1 if some error occurs
         */
        private int postToken(String token) {
            Resources res = getResources();
            String url = API_BASEURL + API_POST_TOKEN;
            int returnVal;

            // build the POST request
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair(
                        res.getString(R.string.api_jsonPostToken_attrNim),
                        res.getString(R.string.api_nim)
                ));
                nameValuePairs.add(new BasicNameValuePair(
                        res.getString(R.string.api_jsonPostToken_attrToken),
                        token
                ));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(httpPost);
                returnVal = response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                e.printStackTrace();
                returnVal = -1;
            }

            return returnVal;
        }
    }
}
