package com.tracker.timothypratama.tomandjerryapplication.Activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tracker.timothypratama.tomandjerryapplication.Model.TrackJerryViewModel;
import com.tracker.timothypratama.tomandjerryapplication.R;

public class GPSTracking extends ActionBarActivity implements OnMapReadyCallback, SensorEventListener{

    private GoogleMap gm;
    private final int zoom = 15;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager sensorManager;
    private RotateAnimation ra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracking);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        image = (ImageView) findViewById(R.id.compassImageView);
        image.setVisibility(View.GONE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Log.d("Visibility", String.valueOf(image.getVisibility()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener((android.hardware.SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((android.hardware.SensorEventListener) this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng jerry = new LatLng(TrackJerryViewModel.getLatitude(), TrackJerryViewModel.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerry, zoom));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.addMarker(new MarkerOptions()
        .title("Jerry")
        .snippet("Jerry is here! Hurry up!")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry))
        .anchor(0.5f,1.0f)
        .position(jerry));
        gm = googleMap;
    }

    public void showJerry(View view) {
        LatLng jerry = new LatLng(TrackJerryViewModel.getLatitude(), TrackJerryViewModel.getLongitude());
        gm.animateCamera(CameraUpdateFactory.newLatLngZoom(jerry,zoom));
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
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void ShowCompass(View view) {
        ImageView compass = (ImageView) findViewById(R.id.compassImageView);
        image.clearAnimation();
        Log.d("Compass Click Tester","Test onclick");
        if(compass.getVisibility() == View.GONE) {
            compass.setVisibility(View.VISIBLE);
            Log.d("compass","view == visible");
        } else if (compass.getVisibility() == View.VISIBLE) {
            compass.setVisibility(View.GONE);
            Log.d("compass"," view == gone");
        }
    }
}
