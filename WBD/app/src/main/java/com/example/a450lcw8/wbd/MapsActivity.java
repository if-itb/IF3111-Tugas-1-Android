package com.example.a450lcw8.wbd;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static java.lang.System.currentTimeMillis;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ProgressDialog pDialog;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    // define the display assembly compass picture
    private ImageView image;

    // URL to get contacts JSON
    private static String url = "http://167.205.32.46/pbd/api/track?nim=13512049";

    // JSON Node names
    private static final String TAG_latitude = "lat";
    private static final String TAG_longitude = "long";
    private static final String TAG_Validuntil = "valid_until";

    static double Longitude, Latitude;
    static String valid_until;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        // Calling async task to get json

        //
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Search for Jerry...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response ", "> " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONObject jsonObj = new JSONObject(jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf("}") + 1));
                    //json.substring("ï»¿");
                    Latitude = jsonObj.getDouble(TAG_latitude);
                    Longitude = jsonObj.getDouble(TAG_longitude);
                    valid_until = jsonObj.getString(TAG_Validuntil);
              } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
                mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 19));


                String formatted_timestamp = "";
            try {
                formatted_timestamp = unixToDate(valid_until); // timestamp in seconds
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("1", formatted_timestamp);
            TextView t = (TextView) findViewById(R.id.tvHeading);
            t.setText("Valid until : " + formatted_timestamp);

        }

}

    @Override
    protected void onResume() {
        super.onResume();
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
        new GetContacts().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    public void moveScan(View view){
        Intent intent = new Intent(this, AndroidQRScanner.class);
        startActivity(intent);
    }

    private String unixToDate(String unix_timestamp) throws ParseException {
        long timestamp = Long.parseLong(unix_timestamp) * 1000;

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d H:mm", Locale.CANADA);
        String date = sdf.format(timestamp);

        return date.toString();
    }


 }
