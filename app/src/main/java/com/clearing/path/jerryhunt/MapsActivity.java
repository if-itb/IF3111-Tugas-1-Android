package com.clearing.path.jerryhunt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // URL to get contacts JSON
    private static String url = "http://167.205.32.46/pbd/";
    private ProgressDialog pDialog;
    private ProgressDialog gDialog;
    private LatLng old_pos;
    private double lat;
    private double lng;
    private long valid;
    private String token;
    private Marker jerry;
    private String SpikeMessage;
    private int returnCode;
    private getResponse sendSpike;
    private SendRequest searchJerry;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchJerry = new SendRequest();
        sendSpike = new getResponse();
        old_pos = new LatLng(lat,lng);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        callAsynchronousTask();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                                Calendar rightNow = Calendar.getInstance();
                                if (valid < rightNow.getTimeInMillis()){
                                    searchJerry = (SendRequest) new SendRequest().execute();
                                }
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(doAsynchronousTask, 0, 1000 * 60 * 2);
    }

    @Override
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

    //product qr code mode
    public void scanQR(View v) {
        try {
                //start the scanning activity from the com.google.zxing.client.android.SCAN intent
                Intent intent = new Intent(ACTION_SCAN);
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException anfe) {
                //on catch, show the download dialog
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
                            token = contents;
                            sendSpike = new getResponse();
                            sendSpike.execute();
//                            Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
//                            toast.show();
                        }
                }
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
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                old_pos = new LatLng(lat,lng);
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        // TODO Auto-generated method stub

                       // mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
                    }
                });
            }
        }
        else if (old_pos.latitude != lat || old_pos.longitude != lng){
            changeMarker();
            old_pos = new LatLng(lat,lng);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng JERRY = new LatLng(lat,lng);
        jerry =mMap.addMarker(new MarkerOptions().position(JERRY)
                .title("Jerry"));

        // Move the camera instantly to hamburg with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JERRY, 10));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    private void changeMarker(){
        LatLng JERRY = new LatLng(lat,lng);
        jerry.setPosition(JERRY);

        // Move the camera instantly to hamburg with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JERRY, 10));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }
    /**
     * Async task class to post json by making HTTP call
     */
    private class getResponse extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            gDialog = new ProgressDialog(MapsActivity.this);
            gDialog.setMessage("Reporting to Spike. Please wait...");
            gDialog.setCancelable(false);
            gDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            List<NameValuePair> resquestBody = new ArrayList<NameValuePair>();
            resquestBody.add(new BasicNameValuePair("nim","13512093"));
            resquestBody.add(new BasicNameValuePair("token",token));

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url+"api/catch", ServiceHandler.POST,resquestBody);
            jsonStr = jsonStr.substring(3);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    SpikeMessage = jsonObj.getString("message");
                    returnCode = jsonObj.getInt("code");

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
            if (gDialog.isShowing()){
                gDialog.dismiss();
            }
            /**
             * Updating parsed JSON data into ListView
             * */
            Toast toast = Toast.makeText(MapsActivity.this,"Return CODE : "+ returnCode+" Spike say : "+SpikeMessage,Toast.LENGTH_LONG);
            toast.show();
        }

    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class SendRequest extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast info :
            Toast toast = Toast.makeText(MapsActivity.this,"Spike says : I found that Jerry location has changed!",Toast.LENGTH_LONG);
            toast.show();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Searching to Jerry.Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url+"api/track?nim=13512093", ServiceHandler.GET);
            jsonStr = jsonStr.substring(3);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    lat = jsonObj.getDouble("lat");
                    lng = jsonObj.getDouble("long");
                    valid = jsonObj.getLong("valid_until") * 1000;

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
            /**
             * Updating parsed JSON data into ListView
             * */
            TextView t,t2,t3;
            String temp;

            t=(TextView)findViewById(R.id.textView2);
            temp = "lat:" + lat;
            t.setText(temp);
            t2=(TextView)findViewById(R.id.textView3);
            temp = "long:" + lng;
            t2.setText(temp);
            t3=(TextView)findViewById(R.id.textView4);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss z");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            temp = "valid:" + sdf.format(valid);
            t3.setText(temp);

            setUpMapIfNeeded();
        }

    }
}
