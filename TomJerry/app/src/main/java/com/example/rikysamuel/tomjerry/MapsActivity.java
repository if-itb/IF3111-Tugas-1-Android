package com.example.rikysamuel.tomjerry;

import android.app.Dialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class MapsActivity extends FragmentActivity implements SensorEventListener,LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double longitude;
    private double latitude;
    private int valid_until;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    String contents;
    TextView text1, text2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle extras = getIntent().getExtras();
        text1 = (TextView) findViewById(R.id.textView4);
        text2 = (TextView) findViewById(R.id.textView5);

        //get the data
        longitude = extras.getDouble("long");
        latitude = extras.getDouble("lat");
        valid_until = extras.getInt("val");

        image = (ImageView) findViewById(R.id.imageView2);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

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

            // Getting Google Play availability status
            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

            // Showing status
            if(status!= ConnectionResult.SUCCESS) { // Google Play Services are not available
                int requestCode = 10;
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
                dialog.show();
            }
            else {
                // Google Play Services are available
                // Getting reference to the SupportMapFragment of activity_main.xml
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

                // Enabling MyLocation Layer of Google Map
                mMap.setMyLocationEnabled(true);

                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Getting the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);

                // Getting Current Location
                Location location = locationManager.getLastKnownLocation(provider);

                if(location!=null){
                    onLocationChanged(location);
                }

                locationManager.requestLocationUpdates(provider, 20000, 0, this);
            }

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {


        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // Setting latitude and longitude in the TextView tv_location
        text1.setText("Latitude:" +  latitude  + ", Longitude:"+ longitude );

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


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        updatePos up = new updatePos();
        up.execute("http://167.205.32.46/pbd/api/track?nim=13512089");
//        LatLng coordinate = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions().position(coordinate).title("Jerry's Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerryicon50)));
//
//        // Move the camera instantly to location with a zoom of 17.
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));
//
//        // Zoom in, animating the camera.
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    private class updatePos extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... uri){
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(uri[0]));
            StringBuilder sb = null;
            HttpResponse response;
            try{
                response = client.execute(request);
                HttpEntity entity = response.getEntity();

                BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                BufferedReader buffRead = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                String line;
                sb = new StringBuilder();
                while((line = buffRead.readLine()) != null){
                    sb.append(line);
                }
            } catch(Exception e){ System.err.println(e);}

            contents = sb.toString();

            return contents;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            LatLng coord = new LatLng(0,0);
            try{
                JSONObject j = new JSONObject(contents);
                latitude = j.getDouble("lat");
                longitude = j.getDouble("long");
                valid_until = j.getInt("valid_until")*1000;
                coord = new LatLng(latitude, longitude);

                new CountDownTimer((valid_until-System.currentTimeMillis()),1000){
                    public void onTick (long r){
                        long ETA = (valid_until - System.currentTimeMillis()) / 1000;
                        TextView t = (TextView) findViewById(R.id.textView6);
                        t.setText(String.valueOf(
                                (ETA/3600) + "jam " + (ETA%3600 / 60) + " menit " + (ETA%3600 %60) + " detik ketika Jerry akan berpindah lokasi"
                        ));
                    }

                    @Override
                    public void onFinish() {
                        setUpMapIfNeeded();
                    }
                }.start();
            } catch(Exception e){e.printStackTrace();}

            mMap.clear();
//            setUpMap();

            mMap.addMarker(new MarkerOptions().position(coord).title("Jerry's Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerryicon50)));

            // Move the camera instantly to location with a zoom of 17.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 17));

            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
        }
    }
}
