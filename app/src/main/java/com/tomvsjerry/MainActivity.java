package com.tomvsjerry;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class MainActivity extends ActionBarActivity implements LocationListener, SensorEventListener, SpikeListener {
    private double lat, lon;
    private Spike spike;
    private float currentDegree;
    private boolean locationValid, currLocationValid, devMode;
    private String nim;
    private Location location;
    private Date validDate;
    private CountDownTimer timer;

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private String provider;

    private ImageView compassImage;
    private TextView txtDistance;
    private TextView txtMessage;
    private TextView txtKeterangan;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spike = new Spike(this, this);
        Bundle bundle = getIntent().getExtras();
        nim = bundle.getString("nim");
        spike.askLocation(nim);
        Toast.makeText(this, "Asking spike for Jerry's location. NIM:" + nim, Toast.LENGTH_LONG).show();

        locationValid = false;
        currLocationValid = false;
        validDate = new Date();

        /*
        if (savedInstanceState != null)
        {
            lat = savedInstanceState.getDouble("lat");
            lon = savedInstanceState.getDouble("lon");
            locationValid = savedInstanceState.getBoolean("locationValid");
            nim = savedInstanceState.getString("nim");
        }
        */

        compassImage = (ImageView) findViewById(R.id.imgCompass);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        txtKeterangan = (TextView) findViewById(R.id.txtKeterangan);
        txtDistance.setText("0");
        txtMessage.setText("");
        txtKeterangan.setText("The arrow points to the north");

        devMode = bundle.getBoolean("devMode");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        provider = locationManager.getBestProvider(criteria, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (!devMode)
        {
            MenuItem devItem = menu.getItem(2);
            devItem.setVisible(false);
            this.invalidateOptionsMenu();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch(id)
        {
            case R.id.action_capture:
                try {
                    intent = new Intent(ACTION_SCAN);
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);

                } catch (ActivityNotFoundException e)
                {
                    Toast.makeText(this, "Please install \"Barcode Scanner\"", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_view_map:
                intent = new Intent(this, MapsActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                startActivity(intent);
                return true;
            case R.id.action_dev_mode:
                intent = new Intent(this, DeveloperActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("nim", nim);
                intent.putExtra("expired", validDate.toString());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putDouble("lat", lat);
        outState.putDouble("lon", lon);
        outState.putBoolean("locationValid", locationValid);
        outState.putString("nim", nim);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == 0)
        {
            if (resultCode == RESULT_OK)
            {
                String contents = intent.getExtras().getString("SCAN_RESULT");
                System.out.println("Barcode Contents:" + contents);
                Toast.makeText(this, "Sending to spike:" + contents, Toast.LENGTH_LONG).show();
                spike.catchRequest(nim, contents);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currLocationValid = true;
        System.out.println("Location found");

        if (locationValid)
        {
            float result[] = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, result);

            txtDistance.setText(String.valueOf(Math.round(result[0])));
            //txtKeterangan.setText("The arrow points to Jerry's Location");
        }

        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        /*
        if (locationValid && currLocationValid)
        {
            degree = getDegrees(location.getLatitude(), location.getLongitude(), lat, lon, degree);
        }
        */

        RotateAnimation ra = new RotateAnimation(currentDegree,
                -degree, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        compassImage.startAnimation(ra);

        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onAnswer() {
        this.lat = spike.getLatitude();
        this.lon = spike.getLongitude();
        Toast.makeText(this, "Spike answered question", Toast.LENGTH_LONG).show();

        validDate = spike.getTimeout();
        txtMessage.setText("Catch Jerry before " + validDate.toString());
        locationValid = true;

        long timeLeft = validDate.getTime() - System.currentTimeMillis();

        if (timer != null)
        {
            timer.cancel();
        }

        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                spike.askLocation(MainActivity.this.nim);
            }
        };

        timer.start();
    }

    private float getDegrees(double lat1, double lon1, double lat2, double lon2, float headX) {

        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        float brng = (float) Math.toDegrees(Math.atan2(y, x));

        // fix negative degrees
        if(brng<0) {
            brng=360-Math.abs(brng);
        }

        return brng - headX;
    }
}
