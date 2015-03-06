package com.example.hp.gogetjerry;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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

public class PetaActivitity extends ActionBarActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peta_activitity);

        //
        image = (ImageView) findViewById(R.id.imageCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        //for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
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
        //get Jerry's position; use api/track
//        JSONObject jsonLatLong = getJSONfromURL("http://167.205.32.46/pbd/api/track?nim=13512080");
//        double Latitude, Longitude;
//        String valid_until;
//        try{
//            Latitude = Double.parseDouble(jsonLatLong.getString("lat")) ;
//            Longitude = Double.parseDouble(jsonLatLong.getString("long"));
//            Log.d("Nilai Lat ", jsonLatLong.getString("lat"));
//            valid_until = jsonLatLong.getString("valid_until");
            mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
//            } catch (JSONException e1) {
//            e1.printStackTrace();

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
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

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

}