package com.example.afik.tomnjerry;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements SensorEventListener {
    private double lat, lon;
    private String valid_until;
    private String dvalid;
    private GoogleMap mMap;
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private final String url_string = "http://167.205.32.46/pbd/api/track?nim=13512077";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (isConnected()) {

            setUpMapIfNeeded();
            // our compass image
            image = (ImageView) findViewById(R.id.imageViewCompass);

            // initialize your android device sensor capabilities
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        else {
            Toast toast = Toast.makeText(this, "You are not connected", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        //tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
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
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
                callAsynchronousTask();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(){

    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new JSONParse().execute("http://167.205.32.46/pbd/api/track?nim=13512077");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 50000); //execute in every 50000 ms
    }

    private class JSONParse extends AsyncTask<String, Void ,LatLng> {
        @Override
        protected LatLng doInBackground(String... args) {
            LatLng loc=null;
            try {
                String jsons = getJSONFromUrl(args[0]);
                JSONObject reader = new JSONObject(jsons);
                lat = reader.getDouble("lat");
                Log.d("lat :", String.valueOf(lat));
                lon = reader.getDouble("long");
                Log.d("lon :", String.valueOf(lon));
                valid_until = reader.getString("valid_until");
                loc = new LatLng(lat,lon);
                System.out.println("lat :" + lat + " long : " + lon);
            } catch (JSONException e) {
                e.printStackTrace();
                loc = new LatLng(0,0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return loc;
        }
        @Override
        protected void onPostExecute(LatLng coordinate) {
            mMap.setMyLocationEnabled(true);
            CameraUpdate Loc = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
            mMap.animateCamera(Loc);
            String snippet_text = "until " + valid_until;
            Marker Jerry = mMap.addMarker(new MarkerOptions()
                    .position(coordinate)
                    .title("Jerry is here")
                    .snippet(snippet_text));
            Jerry.showInfoWindow();
        }
    }

    public String getJSONFromUrl(String... params) throws MalformedURLException {
       String response = "";
       for(String url: params){
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try{
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while((s = br.readLine()) != null){
                    response += s;
                }
            } catch (ClientProtocolException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        response = response.substring(1,response.length());
        Log.d("Response ", response);
        return response;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}

