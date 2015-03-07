package com.example.hp.gogetjerry;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PetaActivitity extends ActionBarActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double Latitude;
    private double Longitude;
    private String valid_until;

    public void setLat (double d){
        this.Latitude = d;
    }

    public void setLong (double d){
        this.Longitude = d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peta_activitity);

        //
        image = (ImageView) findViewById(R.id.imageCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        new GetAPITrack().execute("http://167.205.32.46/pbd/api/track?nim=13512080");
        Log.d("LATITUDE", String.valueOf(Latitude));
        callAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        //for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void callAsyncTask(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask(){

            @Override
            public void run() {
                handler.post(new Runnable(){
                   public void run(){
                       try{
                           setUpMapIfNeeded();
                       } catch (Exception e){
                           e.printStackTrace();
                       }
                   }
                });
            }
        };
        timer.schedule(doAsynchronousTask,0,5000); //execute in every 5 s
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
//                setUpMap();
                new GetAPITrack().execute("http://167.205.32.46/pbd/api/track?nim=13512080");
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
            Log.d("Cek latitude setUpMap", String.valueOf(Latitude));
            Log.d("Cek longitude setUpMap", String.valueOf(Longitude));

            mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Marker"));

    }

    /**
     * A function to get the position of Jerry from api track
     */
    public static JSONObject getJSONfromURL(String url) {
        //initialize
        InputStream in = null;
        String result = "";
        JSONObject jArray = null;

        //http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            in = entity.getContent();
        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
            result = sb.toString();
        } catch(Exception e){
            Log.e("log_tag", "Error converting result "+e.toString());
        }

        //try parse the string to a JSON object
        try{
            jArray = new JSONObject(result);
        } catch (JSONException e){
            Log.e("log_tag", "Error parsing data "+e.toString());
        }

        return jArray;
    }

    @Override
    protected void onPause(){
        super.onPause();
        //stop the listener to save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        //create a rotation animation
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        //duration of animation
        ra.setDuration(210);
        //set the animation after the end of the reservation status
        ra.setFillAfter(true);
        //start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }

    /*attributes added*/
    private ImageView image; //display of compass
    private float currentDegree = 0f; //record the turning
    private SensorManager mSensorManager; //device sensor manager


    /*To get data from endpoint in an asynchronus way*/
    class GetAPITrack extends AsyncTask<String, String, String>{


        @Override
        protected String doInBackground(String... params) {
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
            Log.d("Response ", response);
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            JSONObject jsonObject;
//            double Lat;
//            double Long;
//            String valid;
            try{
                jsonObject = new JSONObject(s);
                Latitude = jsonObject.getDouble("lat");
                Longitude = jsonObject.getDouble("long");
                valid_until = jsonObject.getString("valid_until");
                LatLng jerryPosition = new LatLng(Latitude, Longitude);
                mMap.setMyLocationEnabled(true);

                mMap.addCircle(new CircleOptions().center(jerryPosition).radius(10000));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerryPosition,17));
                MarkerOptions mopt = new MarkerOptions().position(jerryPosition).title("Jerry's here!");
                mopt.icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry));

                mMap.addMarker(mopt);
                Log.d("LAT ", String.valueOf(Latitude));
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}