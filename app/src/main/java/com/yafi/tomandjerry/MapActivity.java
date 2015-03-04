package com.yafi.tomandjerry;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


public class MapActivity extends FragmentActivity implements SensorEventListener{
    final String SERVER = "http://167.205.32.46/pbd";
    private GoogleMap map;
    private Marker marker;
    private LatLng targetPosition = new LatLng(-6.8850447,107.6176397);
    private long validUntil = -1;
    boolean isLocationUpdated;
    Timer timer;
    TimerTask autoUpdateTask;

    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.imageViewCompass);


        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
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

    public void moveCameraToTarget(View v){
        map.animateCamera(CameraUpdateFactory.newLatLng(targetPosition));

    }

    public void moveCameraToMyLocation(View v){
        double lat = map.getMyLocation().getLatitude();
        double lng = map.getMyLocation().getLongitude();
        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
                con.setRequestProperty("Accept-Charset", "UTF-8");
                con.connect();
                int responseCode = con.getResponseCode();

                InputStream is = con.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is,"UTF8"));
                StringBuffer sb = new StringBuffer();
                int chr;
                while ((chr = r.read()) != -1){
                    sb.append(((char) chr));
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
            double lat = targetPosition.latitude;
            double lng = targetPosition.longitude;
            double oldLat = lat, oldLng = lng;
            try {
                Log.d("test",result);
                JSONObject jObject = new JSONObject(result);
                Iterator<?> keys = jObject.keys();
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
                Log.d("lat","new lattitude = " + Double.toString(marker.getPosition().latitude));
                Log.d("lng","new longitude = " + Double.toString(marker.getPosition().longitude));
                if (Math.abs(lat - oldLat) <= 0.0001 && Math.abs(lng - oldLng) <= 0.0001){
                    //dont view toast
                } else {
                    //make notification
                    Toast.makeText(getApplicationContext(),"target location updated",Toast.LENGTH_SHORT).show();
                }
                validUntil = (System.currentTimeMillis()/1000) + 10;
            } catch (JSONException je) {
                je.printStackTrace();
                Toast.makeText(getApplicationContext(), "JSONException:  " + je.getMessage(), Toast.LENGTH_LONG).show();
                //set to default location
                targetPosition = new LatLng(oldLat,oldLng);
            }
        }
    }



}






