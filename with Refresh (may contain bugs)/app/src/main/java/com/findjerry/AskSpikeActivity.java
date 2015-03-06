package com.findjerry;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AskSpikeActivity extends FragmentActivity implements SensorEventListener {

    private Double latitude, longitude;
    private String valid_until;
    private long valid_until_long;
    private long current_time;
    private CountDownTimer timer;
    private TextView timer_text;

    //Map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get data from RefreshTrackingJerryActivity
        Intent extras = getIntent();
        latitude = extras.getDoubleExtra("latitude",0);
        longitude = extras.getDoubleExtra("longitude",0);
        valid_until = extras.getStringExtra("valid_until");
        valid_until_long = extras.getLongExtra("valid_until_long",0);

        setContentView(R.layout.activity_ask_spike);
        setUpMapIfNeeded();

        Date date = new Date();
        current_time = date.getTime();
        valid_until_long = valid_until_long * 1000;
        long duration = valid_until_long - current_time;
        if(duration<=0) {
            duration = 100;
        }

        // refresh part
        timer_text = (TextView) findViewById(R.id.timer);
        timer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalsec = millisUntilFinished / 1000;

                long day = totalsec / (3600 * 24);
                totalsec = totalsec - (day * 3600 * 24);

                long hrs = totalsec / 3600;
                totalsec = totalsec - (hrs * 3600);

                long mnt = totalsec / 60;
                totalsec = totalsec - (mnt * 60);
                timer_text.setText("Elapsed time:\n" +day+"d "+hrs+"h "+mnt+"m "+totalsec+"s\n");
            }

            public void onFinish() {
                Intent intent = new Intent(AskSpikeActivity.this,RefreshTrackingJerryActivity.class);
                timer.cancel();
                finish();
                startActivity(intent);
            }
        };
        timer.start();

        // compass image
        image = (ImageView) findViewById(R.id.compass);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    protected void onResume() {
        super.onResume();

        Date date = new Date();
        current_time = date.getTime();
        long duration = valid_until_long - current_time;
        if(duration<=0) {
            duration = 30000;
        }

        timer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalsec = millisUntilFinished / 1000;

                long day = totalsec / (3600 * 24);
                totalsec = totalsec - (day * 3600 * 24);

                long hrs = totalsec / 3600;
                totalsec = totalsec - (hrs * 3600);

                long mnt = totalsec / 60;
                totalsec = totalsec - (mnt * 60);
                timer_text.setText("Elapsed time:\n" +day+"d "+hrs+"h "+mnt+"m "+totalsec+"s\n");
            }

            public void onFinish() {
                Intent intent = new Intent(AskSpikeActivity.this,RefreshTrackingJerryActivity.class);
                timer.cancel();
                finish();
                startActivity(intent);
            }
        };
        timer.start();

        setUpMapIfNeeded();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
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
        LatLng coordinate = new LatLng(latitude, longitude);
        CameraUpdate jerryLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        mMap.animateCamera(jerryLocation);
        String snippet_text = "until " + valid_until;
        Marker Jerry = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Jerry is here")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry))
                .snippet(snippet_text));
        Jerry.showInfoWindow();
    }

    //compass
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
        // not in use
    }
}
