package com.example.rikysamuel.tomjerry;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements SensorEventListener,LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double longitude;
    private double latitude;
    private int valid_until;

    private ImageView image;
    private long myepoch;
    private boolean lock;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
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

        myepoch = 0;

        setUpMapIfNeeded();
        createCountDownTimer();

        text2.setText("Lat: " + latitude + ", Long: " + longitude + ", valid_until: " + valid_until);

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
            else { // Google Play Services are available
                // Getting reference to the SupportMapFragment of activity_main.xml
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();

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
        LatLng coordinate = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(coordinate).title("Jerry's Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerryicon50)));

        // Move the camera instantly to location with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    private void createCountDownTimer() {
//        new CountDownTimer(valid_until, 1000) {
//            HTTPClass htc = new HTTPClass();
//
//            @Override
//            public void onTick(long millisUntilFinished) {
////                myepoch = System.currentTimeMillis()/1000;
//                myepoch++;
//                text1.setText(String.valueOf(myepoch));
//                if (millisUntilFinished-(myepoch*1000) == 0){
//                    onFinish();
//                }
//            }
//
//            @Override
//            public void onFinish() {
//                @Override
//                public void run() {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            new Updater().execute("http://167.205.32.46/pbd/api/track?nim=13512032");
//                        }
//                    });
//                }
//                myepoch = 100;
//                Toast.makeText(getApplicationContext(),"on finish~",Toast.LENGTH_SHORT);
////                text2.setText("on finish~");
//                System.out.println("on finish~~~");
//
//                htc.setUrl("http://167.205.32.46/pbd/api/track?nim=13512089");
//                String result = htc.doGet();
//
//                JSONObject json = null;
//                try {
//                    System.out.println("Lat: " + latitude);
//                    System.out.println("Long: " + longitude);
//                    System.out.println("Val: " + valid_until);
//                    text2.setText("Lat: " + latitude + ", Long: " + longitude + ", valid_until: " + valid_until);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("valid_until:" + valid_until);
//                if (valid_until == 1425833999){
//                    try{
//                        System.out.println("stopppppp");
//                        Thread.sleep(3);
//                    } catch (Exception e){
//                        System.err.println(e);
//                    }
//                }
//                valid_until = 10000;
//                setUpMapIfNeeded();
//                createCountDownTimer();
//                    TextView text = (TextView) findViewById(R.id.textView5);
//                    text.setText("finish~");
//                    time = 10;
//                    text.setText("Text View");
//                    this.start();
//                try {
//                    JSONObject objek = new TrackingTask().execute().get();
//                    if (objek != null) {
//                        lat = (float) objek.getDouble("lat");
//                        lang = (float) objek.getDouble("long");
//                        deadline = (long) objek.getLong("valid_until")-currtime;
//                        setUpMap();
//                        createCountDownTimer();
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
    }

//    public class UpdateCoord extends AsyncTask<Context, String, String> {
//        HTTPClass httpget = new HTTPClass();
//        private Context context;
//
//        public void parse(String result) {
//            try {
//                JSONObject json = new JSONObject(result);
//                latitude = json.getDouble("lat");
//                longitude = json.getDouble("long");
//                valid_until = json.getInt("valid_until");
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // TODO Auto-generated method stub
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(Context... params) {
//            context = params[0];
//            httpget.setUrl("http://167.205.32.46/pbd/api/track?nim=13512089");
//            String result = httpget.doGet();
//            parse(result);
//            lock = false;
//
//            return result;
//        }
//    }
}
