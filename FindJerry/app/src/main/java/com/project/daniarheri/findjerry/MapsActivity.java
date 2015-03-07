package com.project.daniarheri.findjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
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
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    MarkerOptions mapMarkerMe = new MarkerOptions();
    MarkerOptions mapMarkerJerry = new MarkerOptions();
    TrackJerry tracker = new TrackJerry();
    CatchJerry catcher = new CatchJerry();
    Marker JerryMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        prepareCompass();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener((android.hardware.SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener((android.hardware.SensorEventListener) this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((android.hardware.SensorEventListener) this, mAccelerometer);
        mSensorManager.unregisterListener((android.hardware.SensorEventListener) this, mMagnetometer);
    }

    private void prepareCompass() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.pointer);
    }

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
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
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



    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private void updateMap(float lat, float lon) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lon));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

    private void updateCamera(){
        CameraUpdate center = CameraUpdateFactory.newLatLng(tracker.getLatLing());
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    private void LoopingUpdate(){
            final TextView tV = (TextView) findViewById(R.id.tVinfo);
            new CountDownTimer(tracker.getTimeForUpdate(), 1000) {//CountDownTimer(edittext1.getText()+edittext2.getText()) also parse it to long

                public void onTick(long millisUntilFinished) {
                    tV.setText("Time remaining: " + millisUntilFinished / 1000);
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    mMap.clear();
                    try {
                        TrackJerry NewTracker = new TrackJerry();
                        NewTracker.findJerry("http://167.205.32.46/pbd/api/track?nim=13512064");
                        tracker.setlatLon(NewTracker.getLat(),NewTracker.getLon());
                        NewTracker = null;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tV.setText("Jerry's position updated! ");
                    LoopingUpdate();
                    JerryMarker = mMap.addMarker(mapMarkerJerry
                            .position(tracker.getLatLing())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerjerry)));
                }
            }.start();
    }

    private void setUpMap() {
        mMap.clear();
        try {
            tracker.findJerry("http://167.205.32.46/pbd/api/track?nim=13512064");
            JerryMarker = mMap.addMarker(mapMarkerJerry
                    .position(tracker.getLatLing())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerjerry)));

            updateCamera();
            LoopingUpdate();
           // updateJerrysPosition(tracker.getTimeForUpdate());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void exit(View view) {
        System.exit(0);
    }


    public void findJerry(View view) throws IOException, ExecutionException, InterruptedException {

    }


    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }


    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                try {
                    catcher.catchJerry(contents);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast toast;
                if (catcher.IsSuccess()) {
                    toast = Toast.makeText(this, "Jerry catched! Congratulation Tom..", Toast.LENGTH_LONG);
                    mMap.clear();
                    updateCamera();
                }else
                    toast = Toast.makeText(this, "Jerry already gone! Better luck next time", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
