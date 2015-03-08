package id.web.tkesgar.mousetracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {
	private static final String LOG_TAG = "mousetracker";
	
	/**
	 * Elapsed time (in miliseconds) for a location to be considered significant.
	 */
	private static final long TIME_SIGNIFICANT = 1000 * 30;

	/**
	 * Minimum time interval (in miliseconds) for a location update.
	 */
	private static final long MIN_UPDATE_TIME = 1000 * 5;
	
	/**
	 * Minimum distance (in meters) for a location update.
	 */
	private static final long MIN_UPDATE_DISTANCE = 0;
	
	/**
	 * Current location.
	 * Update using {@link #onLocationChanged(Location)}.
	 */
	private Location location = null;
	
	/**
	 * Current compass rotation, defaults at 0 (N at north).
	 * Update using {@link #setCompassRotation(int)}.
	 */
	private float compassRotation = 0;

	private LocationManager locationManager;
	private SensorManager sensorManager;
	private Sensor geomagneticSensor;
	
    private TextView textCurrentLatitude;
    private TextView textCurrentLongitude;
    private ImageView imageCompass;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize view and view references.
        setContentView(R.layout.activity_main);
        textCurrentLatitude  = (TextView) findViewById(R.id.latitude_current);
        textCurrentLongitude = (TextView) findViewById(R.id.longitude_current);
        imageCompass = (ImageView) findViewById(R.id.image_compass);
        
        // Get Android services, managers, and sensors.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        // Initialize location from last known position (might still null if location is disabled).
    	setLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Register the location listener.
        // removeUpdates() should be called at onPause().
        locationManager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER,
        		MIN_UPDATE_TIME,
        		MIN_UPDATE_DISTANCE,
        		this
        	);
        
        // Register the geomagnetic sensor listener.
        // unregisterListener() should be called at onPause().
        sensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_UI);
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the location listener.
        // requestLocationUpdates() should be called at onResume().
        locationManager.removeUpdates(this);
        
        // Register the geomagnetic sensor listener.
        // unregisterListener() should be called at onPause().
        sensorManager.unregisterListener(this, geomagneticSensor);
    }

	@Override
	public void onProviderEnabled(String provider) {
		Log.v(LOG_TAG, provider + " enabled");
		
		// Notify the user via toast.
		Toast.makeText(
				getApplicationContext(),
				getString(R.string.msg_location_enabled),
				Toast.LENGTH_SHORT
			).show();
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.v(LOG_TAG, provider + " disabled");
		
		// Notify the user via toast.
		Toast.makeText(
				getApplicationContext(),
				getString(R.string.msg_location_disabled),
				Toast.LENGTH_SHORT
			).show();
	}
	
	@Override
	public void onLocationChanged(Location newLocation) {
		Log.v(LOG_TAG, "location changed");
		
		// Check whether new location is better, then set it if necessary.
		if (isBetterLocation(newLocation, location)) {
			setLocation(newLocation);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		switch (status) {
		case LocationProvider.AVAILABLE:
			Log.v(LOG_TAG, provider + " available");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.v(LOG_TAG, provider + " temporarily unavailable");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			Log.v(LOG_TAG, provider + " out of service");
			break;
		default:
			Log.d(LOG_TAG, String.format("%s: %d", provider, status));
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(LOG_TAG, String.format("%s accuracy %d", sensor.getName(), accuracy));
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Click method for "Track" button defined in layout.
	 */
	public void buttonTrack(View button) {
		Log.i(LOG_TAG, "track");
	}

	/**
	 * Click method for "Scan" button defined in layout.
	 */
	public void buttonScan(View button) {
		Log.i(LOG_TAG, "scan");
	}

	/**
	 * Click method for "Catch" button defined in layout.
	 */
	public void buttonCatch(View button) {
		Log.i(LOG_TAG, "catch");
	}

	/**
	 * Sets the location and updates the views.
	 */
	private void setLocation(Location location) {
		
		this.location = location;
		if (location != null) {
			Log.v(LOG_TAG, String.format("location = (lat=%.6f, long=%.6f)", location.getLatitude(), location.getLongitude()));
			textCurrentLatitude.setText(String.format("%.6f", location.getLatitude()));
			textCurrentLongitude.setText(String.format("%.6f", location.getLongitude()));
		}
	}

	/**
	 * Sets the compass rotation and updates the view.
	 */
	public void setCompassRotation(float compassRotation) {
		this.compassRotation = compassRotation;
		imageCompass.setRotation(compassRotation);
	}

    /**
     * Determines whether one Location reading is better than the current Location fix.
     * 
     * Source: <a href="http://developer.android.com/guide/topics/location/strategies.html">Location Strategies</a>
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        
    	if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_SIGNIFICANT;
        boolean isSignificantlyOlder = timeDelta < -TIME_SIGNIFICANT;
        boolean isNewer = timeDelta > 0;

        // If it's been more than one minutes since the current location,
        // use the new location because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than one minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(
        		location.getProvider(),
                currentBestLocation.getProvider()
            );

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same.
     * 
     * Source: <a href="http://developer.android.com/guide/topics/location/strategies.html">Location Strategies</a>
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
        	return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
