package com.example.test;

/* Import for Compass */
import java.io.IOException;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;



/* Import for Maps */
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;





/* Import for JSON*/
import org.json.*;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	/* HTTP Request */
	private HTTPRequestGet HTTPreq = new HTTPRequestGet();
	private TextView matalowh;
	
	/* Map */
	private GoogleMap map;
	
	/* Compass */
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
	
	/* Activities */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /* Compass Initialization */
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    mPointer = (ImageView) findViewById(R.id.pointer);
        
	    /* JSON parsing*/
	   
	    
        /* Map Initialization */
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        /* Map process */
        if (map!=null){
        	map.setMyLocationEnabled(true);
        	map.getUiSettings().setRotateGesturesEnabled(false);
        	map.getUiSettings().setCompassEnabled(false);
        	
        	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        	
            if (location != null)
            {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 0));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(15)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
            else {
            	matalowh = (TextView) findViewById(R.id.dummy2);
            	matalowh.setText("matalowh");
            }
        }
    }
    
    protected void onResume() {
	    super.onResume();
	    map.setMyLocationEnabled(true);
	    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
	}
	 
	protected void onPause() {
	    super.onPause();
	    map.setMyLocationEnabled(false);
	    mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
	}
    
    @Override
	public void onSensorChanged(SensorEvent event) {
    	/* Compass Process */
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
		// TODO Auto-generated method stub
		
	}


}