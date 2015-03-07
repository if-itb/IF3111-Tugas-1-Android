package com.example.steve.tubesandroid1;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class mapGoogleJerry2 extends FragmentActivity implements SensorEventListener{
    /**
     * Atribut kelas
     *
     */
    private double latitudeJerry;                   // Posisi latitude Jerry
    private double longitudeJerry;                  // Posisi longitude Jerry
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private SensorManager sensorMagnetic;           // Object yang menyediakan service sensor android
    private Sensor accelerometer;                   // Object khusus untuk mendeteksi sensor berupa percepatan device
    private Sensor magnetometer;                    // Object khusus untuk mendeteksi sensor berupa gaya gravitasi
    private ImageView compassIndicator;             // Gambar kompas yang dirender pada peta
    private boolean setAccelerometer = false;       // Keadaan awal : sensor accelerometer tidak menyala
    private boolean setMagnetometer = false;        // Keadaan awal : sensor magnetic tidak mnyeala
    private float[] mLastAccelerometer = new float[3];      // Array yang menampung parameter percepatan device
    private float[] mLastMagnetometer = new float[3];       // Array yang menampung parameter sensor magnetic
    private float[] mOrientation = new float[3];             // Array yang menampung paramter arah panah kompas
    private float mCurrentDegree = 0f;                      // Derajat kemiringan kompas sementara
    private float[] mR = new float[9];                      // Matriks untuk menampung matriks hasil rotasi kompas
    private LocationManager gpsActivation;                  // Object untuk membuat menampilkan lokasi dengan GPS dan network provider
    private LocationListener gpsListener;                   // Object untuk menghandle event dari gpsActivation location manager
    boolean isGPSEnabled = false;                           // Boolean yang menandakan apakah GPS aktif/tidak
    boolean isNetworkEnabled = false;                       // Boolean yang menandakan apakah network connection aktif/tidak

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_google_jerry2);

       /* // Setting koordinat Jerry
        Bundle lokasiJerry = getIntent().getExtras();
        latitudeJerry = Double.parseDouble(lokasiJerry.getString("latitude"));
        longitudeJerry = Double.parseDouble(lokasiJerry.getString("longitude"));

        // Setting GPS manager dan listener serta koordinat Jerry/Device awal
        gpsActivation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new LocationListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onLocationChanged(Location location) {
                // View lokasi kita dan lokasi Jerry at google map
                mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if (mMap !=null) {
                    LatLng koordinatJerry = new LatLng(latitudeJerry,longitudeJerry);
                    LatLng ourDevice = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(koordinatJerry).title("Jerry is here. Hurry!"));
                    mMap.addMarker(new MarkerOptions().position(ourDevice).title("You are here."));
                }
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
        };*/

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        sensorMagnetic.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorMagnetic.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMagnetic.unregisterListener(this, accelerometer);
        sensorMagnetic.unregisterListener(this, magnetometer);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
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
        /*
        isGPSEnabled = gpsActivation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = gpsActivation.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            if (isNetworkEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.NETWORK_PROVIDER, 6000, 10, gpsListener);
            }
            if (isGPSEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.GPS_PROVIDER,2000,10,gpsListener);
            }
        }*/
        LatLng asal = new LatLng(0,0);
        mMap.addMarker(new MarkerOptions().position(asal).title("Asal"));
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link android.hardware.SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        // Deteksi sensor accelerometer dan magnetometer
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            setAccelerometer = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            setMagnetometer = true;
        }

        // Gambar kompas jika terjadi perubahan environment dari kedua sensor
        if (setAccelerometer && setMagnetometer) {
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

            compassIndicator.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }*/
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link android.hardware.SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
