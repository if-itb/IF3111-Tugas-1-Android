package com.christangga.if3111_jerrycatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private static final int CAMERA_DISTANCE = 17;

    private Handler handler = new Handler();

    // maps
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double latitude, longitude;
    private long valid_until;

    // compass
    private ImageView imageViewCompassSmall, imageViewCompassBig;
    private SensorManager mSensorManager;
    private float currentDegree = 0f;

    // scan
    private ImageButton buttonRefresh, buttonScan;

    private static AlertDialog.Builder showDialog(final Activity activity, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return downloadDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // maps
        buttonRefresh = (ImageButton) findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpMapIfNeeded();
            }
        });
        setUpMapIfNeeded();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (!isNetworkAvailable()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                buttonRefresh.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // compass
        imageViewCompassSmall = (ImageView) findViewById(R.id.imageViewCompassSmall);
        imageViewCompassBig = (ImageView) findViewById(R.id.imageViewCompassBig);
        imageViewCompassSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                imageViewCompassBig.setVisibility(View.VISIBLE);
            }
        });
        imageViewCompassBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewCompassSmall.setVisibility(View.VISIBLE);
                v.setVisibility(View.INVISIBLE);
            }
        });
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // scan
        buttonScan = (ImageButton) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                i.putExtra("SCAN_MODE", "QR_CODE_MODE");
                if (isNetworkAvailable()) {
                    if (i.resolveActivityInfo(getPackageManager(), 0) != null) {
                        startActivityForResult(i, 0);
                    } else {
                        showDialog(MapsActivity.this, "No scanner found", "Download Barcode Scanner by ZXing Team?", "Yes", "No").show();
                    }
                } else {
                        Toast.makeText(getApplicationContext(), "No internet access", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // maps
        setUpMapIfNeeded();

        // compass
        imageViewCompassSmall.setVisibility(View.VISIBLE);
        imageViewCompassBig.setVisibility(View.INVISIBLE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // compass
        imageViewCompassSmall.setVisibility(View.INVISIBLE);
        imageViewCompassBig.setVisibility(View.INVISIBLE);
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // compass
        imageViewCompassSmall.setVisibility(View.INVISIBLE);
        imageViewCompassBig.setVisibility(View.INVISIBLE);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(0);
        ra.setFillAfter(true);

        if (imageViewCompassSmall.getVisibility() == View.VISIBLE) {
            imageViewCompassSmall.startAnimation(ra);
        }
        if (imageViewCompassBig.getVisibility() == View.VISIBLE) {
            imageViewCompassBig.startAnimation(ra);
        }
        ra.setFillAfter(false);

        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
        }

        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (isNetworkAvailable()) {
            buttonRefresh.setVisibility(View.INVISIBLE);
            HttpGetTrack httpGetTrack = new HttpGetTrack();
            httpGetTrack.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet access", Toast.LENGTH_SHORT).show();
            buttonRefresh.setVisibility(View.VISIBLE);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String token = intent.getStringExtra("SCAN_RESULT");

                if (isNetworkAvailable()) {
                    HttpPostCatch httpPostCatch = new HttpPostCatch();
                    httpPostCatch.execute(token);
                } else {
                    Toast.makeText(getApplicationContext(), "No internet access", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "Failed to scan QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class HttpGetTrack extends AsyncTask<Void, Void, JSONObject> {

        private ProgressDialog progressBar = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setCancelable(false);
            progressBar.setMessage("Tracking Jerry");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setIndeterminate(true);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512019");
            try {
                HttpResponse response = client.execute(request);

                BufferedReader rd = new BufferedReader
                        (new InputStreamReader(response.getEntity().getContent()));

                String message = "";
                String line;
                while ((line = rd.readLine()) != null) {
                    message += line;
                }

                Log.d("api/track", message);
                return new JSONObject(message);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            try {
                if (jsonObject != null) {
                    latitude = Double.parseDouble(jsonObject.getString("lat"));
                    longitude = Double.parseDouble(jsonObject.getString("long"));
                    valid_until = jsonObject.getLong("valid_until") * 1000;

                    // testing
//                    valid_until = Long.parseLong("1425458700000");
//                    while (valid_until < System.currentTimeMillis()) {
//                        valid_until += 5000;
//                        latitude = new Random().nextInt(180) - 90;
//                        longitude = new Random().nextInt(360) - 180;
//                    }
//                    Log.d("valid_until", new Date(valid_until).toString());

                    new CountDownTimer(valid_until - System.currentTimeMillis(), 1000) {

                        public void onTick(long millisUntilFinished) {
                            Log.d("refresh_in", String.valueOf((valid_until - System.currentTimeMillis()) / 1000) + " seconds");
                        }

                        public void onFinish() {
                            setUpMap();
                        }
                    }.start();

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), CAMERA_DISTANCE);
                    mMap.animateCamera(cameraUpdate);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry"));
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get Jerry location!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressBar.dismiss();
        }
    }

    private class HttpPostCatch extends AsyncTask<String, Void, Integer> {

        ProgressDialog progressBar = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            progressBar.setCancelable(false);
            progressBar.setMessage("Catching Jerry");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setIndeterminate(true);
            progressBar.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
            post.setHeader("Content-type", "application/json");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nim", "13512019");
                jsonObject.put("token", params[0]);
                post.setEntity(new StringEntity(jsonObject.toString()));

                HttpResponse response = client.execute(post);
                int code = response.getStatusLine().getStatusCode();

                Log.d("api/catch", String.valueOf(code));
                return code;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer code) {
            progressBar.dismiss();
            if (code == 200) {
                Toast.makeText(getApplicationContext(), "Congratulations! Jerry is captured!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Error while catching Jerry!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
