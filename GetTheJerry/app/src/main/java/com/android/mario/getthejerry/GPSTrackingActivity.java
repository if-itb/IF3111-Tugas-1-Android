package com.android.mario.getthejerry;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
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
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mario on 07/03/2015.
 */
public class GPSTrackingActivity extends ActionBarActivity implements SensorEventListener, OnMapReadyCallback {

    Timer timer;
    final int update_rate = 120000; // in milliseconds

    private ImageView compass;
    private float currentDegree = 0f;
    private SensorManager sensorManager;
    private RotateAnimation ra;

    private GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracking);

        timer = new Timer();
        startUpdate();

        compass = (ImageView) findViewById(R.id.compass);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startUpdate(){
        final Handler handler = new Handler();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new Updater().execute("http://167.205.32.46/pbd/api/track?nim=13512016");
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, update_rate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng pos = new LatLng(ViewPosition.getLatitude(), ViewPosition.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 19));
        googleMap.addMarker(new MarkerOptions()
                .title("Jerry")
                .snippet("Get Jerry Here!")
                .position(pos));
        gMap = googleMap;
    }

    public void showJerry(View view){
        LatLng pos = new LatLng(ViewPosition.getLatitude(), ViewPosition.getLongitude());
        gMap.addMarker(new MarkerOptions()
                .title("Jerry")
                .snippet("Get Jerry Here!")
                .position(pos));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 19));
    }

    class Updater extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            for (String url : params){
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = bufferedReader.readLine()) != null){
                        response += s;
                    }

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            JSONObject jsonObject;
            double Latitude, Longitude;
            String valid;

            try{
                jsonObject = new JSONObject(s);
                Latitude = jsonObject.getDouble("lat");
                Longitude = jsonObject.getDouble("long");
                valid = jsonObject.getString("valid_until");

                ViewPosition.setLatitude(Latitude);
                ViewPosition.setLongitude(Longitude);
                ViewPosition.setValidUntil(valid);

                TextView lat = (TextView) findViewById(R.id.lat);
                TextView longg = (TextView) findViewById(R.id.lon);
                TextView valid_until = (TextView) findViewById(R.id.valid);
                lat.setText("lat  : "+String.valueOf(Latitude));
                longg.setText("long :"+String.valueOf(Longitude));
                valid_until.setText(unixToDate(valid));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String unixToDate(String unix_timestamp) {
            long timestamp = Long.parseLong(unix_timestamp) * 1000;

            TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
            sdf.setTimeZone(timeZone);
            String date = sdf.format(timestamp);

            return date.toString();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        ra.setDuration(210);
        ra.setFillAfter(true);
        compass.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        timer = new Timer();
        startUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gpstracking, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void catchJerry(View view){
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }
}
