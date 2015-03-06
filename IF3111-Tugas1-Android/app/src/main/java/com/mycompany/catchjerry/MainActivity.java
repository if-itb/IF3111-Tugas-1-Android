package com.mycompany.catchjerry;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends FragmentActivity implements SensorEventListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private boolean destroyed;
    private double longitude;
    private double latitude;
    private String valid_until;
    private Marker mapMarker;
    private Button button;

    private long epoch;

    TextView tvHeading;

    ConnectTask connectTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        destroyed = false;
        setUpMapIfNeeded();
        setQRButton();



        connectTask = new ConnectTask();
        connectTask.execute(getApplicationContext());
        image = (ImageView) findViewById(R.id.imageViewCompass);
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected  void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected  void onDestroy(){
        destroyed = true;
        connectTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if(mapMarker!=null)
            mapMarker.remove();
        mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mapMarker.showInfoWindow();
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.setMyLocationEnabled(true);
    }

    private void setQRButton(){
        button = (Button) findViewById(R.id.QR_scan);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Toast.makeText(getApplicationContext(), scanResult.getContents(), Toast.LENGTH_SHORT).show();
            Log.d("scan result", "[SCAN] "+scanResult.getContents());
            try {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendPost(scanResult.getContents());
                        } catch (Exception e) {
                            Log.d("[POST]", "[POST] thread Exception");
                        }
                    }
                });
                thread.start();
            } catch (Exception e) {
                Log.d("cannot send post", "[POST] cannot make post request");
            }
        }
    }

    /*
        Http Post reqeust to catch Jerry
     */
    private void sendPost(String token) throws Exception  {
        JSONObject json = new JSONObject();
        json.put("nim", "13512001");
        json.put("token", token);

        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch");
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            Log.d("[POST]", "[POST] Response "+response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            Log.d("[POST]", "[POST] send post caught exception");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
// get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        RotateAnimation ra = new RotateAnimation(
                currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
        0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);

        currentDegree = -degree;



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class ConnectTask extends AsyncTask<Context,String,String> {
        protected void onPrexecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            HttpClient request = new DefaultHttpClient();
            HttpGet address = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512001");
            HttpResponse response;

            String res = "";

            while(!destroyed)
                try {
                    response = request.execute(address);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    res = reader.readLine();

                    JSONObject object = new JSONObject(res);

                    longitude = Double.valueOf(object.getString("long"));
                    latitude = Double.valueOf(object.getString("lat"));
                    valid_until = String.valueOf(object.getString("valid_until"));
                    Log.d("hasil","get" + longitude +" "+ latitude + " " + valid_until);

                    epoch = Long.valueOf(valid_until)-(System.currentTimeMillis()/1000) -1 ;

                    if(epoch>0){
                        Thread thread = new Thread(){
                        public void run(){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mapMarker!=null)
                                            mapMarker.remove();

                                        mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Jerry").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                        mapMarker.showInfoWindow();
                                        Log.d("halo","map udh keganti");
                                        mMap.getUiSettings().setMapToolbarEnabled(true);
                                        mMap.setMyLocationEnabled(true);

                                        View mapView = (View) findViewById(R.id.map);
                                        mapView.invalidate();
                                        mapView.forceLayout();
                                        //Toast.makeText(getApplicationContext(), "New Jerry Location!", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                    };
                        thread.start();
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (epoch>0) {
                    Log.d("sleep", "[GET] background sleeps");
                    try {
                                 /* sleep thread when not needed */
                        Thread.sleep(epoch * 1000);
                    } catch (InterruptedException e) {
                        Log.d("stacktrace", "Stack Trace "+e.toString());
                    }
                }


            return String.valueOf("true");
        }
    }
}
