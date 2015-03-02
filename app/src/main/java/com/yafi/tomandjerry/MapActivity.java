package com.yafi.tomandjerry;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


public class MapActivity extends Activity {
    final String SERVER = "http://167.205.32.46/pbd";
    private GoogleMap map;
    private Marker marker;
    private LatLng targetPosition = new LatLng(-6.8850447,107.6176397);
    private long validUntil = -1;
    boolean isLocationUpdated;
    Timer timer;
    TimerTask autoUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //set default position
        marker = map.addMarker(new MarkerOptions()
                .title("Target")
                .position(targetPosition));
        map.moveCamera(CameraUpdateFactory.zoomTo(16));
        map.moveCamera(CameraUpdateFactory.newLatLng(targetPosition));
        map.setMyLocationEnabled(true);
        //call ajax
        updateTargetLocation();
        initAutoUpdateTask();
    }

    @Override
    protected void onResume(){
        super.onResume();
        startAutoUpdateTask();
    }

    private void updateTargetLocation() {
        new UpdateTargetLocationTask().execute("");
    }

    private void startAutoUpdateTask(){
        //schedule the timer, after the first 5000ms the TimerTask will run every 3000ms
        timer.schedule(autoUpdateTask,1000,1000);
    }

    private void initAutoUpdateTask(){
        timer = new Timer();
        autoUpdateTask = new TimerTask() {
            @Override
            public void run() {
                long timeNow = (new Date()).getTime()/1000;
                //in update target location task, valid until will be update each 10 seconds
                if (validUntil != -1 && timeNow > validUntil){
                    new UpdateTargetLocationTask().execute("");
                }
            }
        };
    }

    private class UpdateTargetLocationTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d("ajax","call update location");
            String url = SERVER+"/api/track?nim=13512014";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");

                con.connect();
                int responseCode = con.getResponseCode();

                InputStream is = con.getInputStream();
                StringBuffer sb = new StringBuffer();
                while (is.available() > 0){
                    sb.append(((char) is.read()));
                }
                return sb.toString();
            } catch (MalformedURLException mue){
                return null;
            } catch (IOException ioe) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObject = new JSONObject(result);
                Iterator<?> keys = jObject.keys();
                double lat = targetPosition.latitude;
                double lng = targetPosition.longitude;
                double oldLat = lat, oldLng = lng;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = jObject.getString(key);
                    if (key.equals("lat")){
                        lat = Double.parseDouble(value);
                    } else if (key.equals("long")){
                        lng = Double.parseDouble(value);
                    } else if (key.equals("valid_until")){
                        validUntil = Long.parseLong(value);
                    }
                }
                targetPosition = new LatLng(lat,lng);
                map.moveCamera(CameraUpdateFactory.newLatLng(targetPosition));
                marker.setPosition(targetPosition);
//                Log.d("lat","target lattitude = " + Double.toString(targetPosition.latitude));
//                Log.d("lng","target longitude = " + Double.toString(targetPosition.longitude));
//                Log.d("lat","new lattitude = " + Double.toString(marker.getPosition().latitude));
//                Log.d("lng","new longitude = " + Double.toString(marker.getPosition().longitude));
                if (Math.abs(lat - oldLat) <= 0.0001 && Math.abs(lng - oldLng) <= 0.0001){
                    //dont view toast
                } else {
                    //make notification
                    Toast.makeText(getApplicationContext(),"target location updated",Toast.LENGTH_SHORT).show();
                }
                validUntil = (System.currentTimeMillis()/1000) + 10;
            } catch (JSONException je) {
                Toast.makeText(getApplicationContext(), "JSONException: get json" + result, Toast.LENGTH_LONG).show();
            }
        }
    }

}






