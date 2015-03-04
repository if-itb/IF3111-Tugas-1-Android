package com.luthfihm.jerrytracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.InputStream;


public class MainActivity extends ActionBarActivity {
    private static SensorManager sensorService;
    private Sensor sensor;
    private ImageView compass;
    private TextView textView;
    private float currentDegree;
    private GoogleMap mMap;
    private Marker currentLocation;
    private Marker jerryLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compass = (ImageView) findViewById(R.id.compass);
        currentDegree = 0f;
        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        textView = (TextView) findViewById(R.id.textView);
        setUpMap();
        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float azimuth = event.values[0];
            currentDegree = -azimuth;
            compass.setRotation(currentDegree);
            updateCamera(azimuth);
        }
    };


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
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpMap()
    {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }

        Jerry jerry = null;
        try {
            jerry = new Jerry("13512100");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        textView.setText("You have time until :\n"+jerry.getTime()+"\nLet's catch Jerry Tom!");
        currentLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.tom)));
        jerryLocation = mMap.addMarker(new MarkerOptions().position(jerry.getLatLng()).title("Jerry's here!").snippet("Catch him!").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)));
        updateCamera(0);

    }

    public void updateCamera(float bearing) {
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation)
        {
            currentLocation.setPosition(new LatLng(gps.getLatitude(),gps.getLongitude()));
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(gps.getLatitude(), gps.getLongitude()))
                    .bearing(bearing).tilt(30f).zoom(18f).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
        }

    }
}
