package com.example.steve.tubesandroid1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

/*if (gpsActivation != null) {
    ourDeviceLocation = gpsActivation.getLastKnownLocation(gpsActivation.NETWORK_PROVIDER);
    if (ourDeviceLocation != null) {
        ourLatitude = ourDeviceLocation.getLatitude();
        ourLongitude = ourDeviceLocation.getLongitude();
    }
}*/
public class mapGoogleJerry extends FragmentActivity  {
    /**
     * Atribut kelas
     *
     */
    private double latitudeJerry;                   // Posisi latitude Jerry
    private double longitudeJerry;                  // Posisi longitude Jerry
    private GoogleMap lokasi;                       // Object mapview google
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_google_jerry);

        // Setting koordinat Jerry
        /*Bundle lokasiJerry = getIntent().getExtras();
        latitudeJerry = Double.parseDouble(lokasiJerry.getString("latitude"));
        longitudeJerry = Double.parseDouble(lokasiJerry.getString("longitude"));*/

        latitudeJerry = Double.parseDouble(lokasiJerry.latitudeJerry);
        longitudeJerry = Double.parseDouble(lokasiJerry.longitudeJerry);

        // View lokasi kita dan lokasi Jerry at google map
        lokasi = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (lokasi!=null) {
            LatLng koordinatJerry = new LatLng(latitudeJerry,longitudeJerry);
            LatLng ourDevice = new LatLng(lokasi.getMyLocation().getLatitude(),lokasi.getMyLocation().getLongitude());
            lokasi.addMarker(new MarkerOptions().position(koordinatJerry).title ("Jerry is here. Hurry!"));
            lokasi.addMarker(new MarkerOptions().position(ourDevice).title("You are here."));
        }
        /*
        // Setting GPS manager dan listener serta koordinat Jerry/Device awal
        gpsActivation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // View lokasi kita dan lokasi Jerry at google map
                lokasi = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                if (lokasi!=null) {
                    LatLng koordinatJerry = new LatLng(latitudeJerry,longitudeJerry);
                    LatLng ourDevice = new LatLng(location.getLatitude(),location.getLongitude());
                    lokasi.addMarker(new MarkerOptions().position(koordinatJerry).title ("Jerry is here. Hurry!"));
                    lokasi.addMarker(new MarkerOptions().position(ourDevice).title("You are here."));
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
        };

        isGPSEnabled = gpsActivation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = gpsActivation.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            if (isNetworkEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.NETWORK_PROVIDER, 6000, 10, gpsListener);
            }
            if (isGPSEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.GPS_PROVIDER,2000,10,gpsListener);
            }
        }

        // Setting gambar kompas
        compassIndicator = (ImageView) findViewById(R.id.kompas);

        // Inisialisasi sensor magnetic
        sensorMagnetic = (SensorManager) getSystemService(SENSOR_SERVICE);*/
    }
    /*

    @Override
    protected void onResume() {
        super.onResume();
        //sensorMagnetic.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        //sensorMagnetic.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        isGPSEnabled = gpsActivation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = gpsActivation.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            if (isNetworkEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.NETWORK_PROVIDER, 6000, 10, gpsListener);
            }
            if (isGPSEnabled) {
                gpsActivation.requestLocationUpdates(gpsActivation.GPS_PROVIDER,2000,10,gpsListener);
            }
        }
    }
    */

    /**
     * Pause sensor dan location sensor saat activity pause
     */
    /*
    @Override
    protected void onPause() {
        super.onPause();
       // sensorMagnetic.unregisterListener(this, accelerometer);
       // sensorMagnetic.unregisterListener(this, magnetometer);
        isGPSEnabled = false;
        isNetworkEnabled = false;
    }
    */
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
    /*
    @Override
    public void onAccuracyChanged (Sensor sensor, int accuracy) {

    }
    */
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
    /*
    @Override
    public void onSensorChanged(SensorEvent event) {
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
        }
    }*/
}
