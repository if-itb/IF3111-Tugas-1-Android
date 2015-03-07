package com.edmundophie.jerrytracker;

import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity implements SensorEventListener{
    private SensorManager sensorManager = null;
    private Sensor magneticField, accelerometer = null;
    private float currentDegree = 0f;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private ImageView compassView;
    private TextView valMagneticX, valMagneticY, valMagneticZ;
    public static TextView valLatitude, valLongitude, valExpire, valResponse;
    public static JerryLocation jerryLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassView = (ImageView) findViewById(R.id.compassView);
        MainActivity.valLatitude = (TextView) findViewById(R.id.valLatitude);
        MainActivity.valLongitude = (TextView) findViewById(R.id.valLongitude);
        MainActivity.valExpire = (TextView) findViewById(R.id.valExpire);
        valResponse = (TextView) findViewById(R.id.valResponse);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Get Jerry's location
        jerryLocation = new JerryLocation();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        updateJerryLocation();
                    }
                },0,2000, TimeUnit.MILLISECONDS
        );
    }


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

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values.clone();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values.clone();
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic);

                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float azimuthInRadians = orientation[0];
                    float azimuthInDegress = (float) ((Math.toDegrees(azimuthInRadians)+360)%360);

                    RotateAnimation ra = new RotateAnimation(currentDegree, -azimuthInDegress, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(250);
                    ra.setFillAfter(true);

                    compassView.startAnimation(ra);
                    currentDegree = -azimuthInDegress;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onClick(View view){
        // Call map activity
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void updateJerryLocation() {
        try {
            String result = new RetrieveJerryLocationTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void scanQR(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
            intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
            startActivityForResult(intent, 0);
        } catch (Exception ex) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                // Send token to server
                try {
                    String result = new SendTokenTask().execute(contents).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
