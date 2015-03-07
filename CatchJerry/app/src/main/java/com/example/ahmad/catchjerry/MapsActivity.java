package com.example.ahmad.catchjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    // Untuk peta
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng jerryPos;
    private Long validTime;
    private Long dateNow;

    // Untuk kompas
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

    // Untuk QR code
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private int response;
    private String contents;
    private JSONObject postResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Peta
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        setJerryPos();


        // Kompas
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.compass);

        // QR Code
        response = 0;

        mMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        LatLng myCoordinates = new LatLng(latitude, longitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myCoordinates, 12);
        mMap.animateCamera(yourLocation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Peta
        setUpMapIfNeeded();

        // Kompas
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void scanQR(View V){
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
        catch(ActivityNotFoundException a){
            // not supported yet
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo ){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try{
                    act.startActivity(intent);
                }
                catch (ActivityNotFoundException anfe){

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }) ;
        return downloadDialog.show();
    }

    private static AlertDialog showCatchDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonOK){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setNeutralButton(buttonOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }) ;
        return downloadDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode==0){
            if(resultCode == RESULT_OK){
                contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                new AsyncTaskPostData().execute();
                while(response==0) {
                    //busy waiting;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String message, title;
                if (response == 200) {
                    title = "You caught him!";
                    message = "status: 200 OK";
                } else {
                    if (response == 400) {
                        title = "Missing parameter";
                        message = "status: 400 MISSING PARAMETER";
                    } else {
                        if (response == 403) {
                            title = "No, no, wrong answer!";
                            message = "status: 403 FORBIDDEN";
                        } else {
                            title = "Unknown response";
                            message = "status: UNKNOWN";
                        }
                    }
                }
                response = 0;

                showCatchDialog(MapsActivity.this, title, message, "OK").show();

            }
        }
    }

    public class AsyncTaskPostData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                postResponse  = JsonParser.postData(contents);
                Log.d("Result", postResponse.toString());
                response = postResponse.getInt("code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.

        }
        else{
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    public void setUpMap(View view) {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        new AsyncTaskParseJson().execute();
        while (jerryPos == null);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(jerryPos)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        jerryPos = null;
    }

    public void setJerryPos() {
        new AsyncTaskParseJson().execute();
        while (jerryPos == null || validTime == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        dateNow = System.currentTimeMillis();

        new CountDownTimer(validTime*1000 - dateNow, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //((TextView)findViewById(R.id.countdown)).setText((int) (millisUntilFinished/1000));
                Long seconds = millisUntilFinished/1000;
                Long elapsedTimeHour = seconds/3600;
                Long elapsedTimeMinute = (seconds-(elapsedTimeHour*3600))/60;
                Long elapsedTimeSecond = seconds-(elapsedTimeHour*3600)-(elapsedTimeMinute*60);
                String elapsedTime = Long.toString(elapsedTimeHour) + "h " + Long.toString(elapsedTimeMinute) + "m " +
                        Long.toString(elapsedTimeSecond) + "s";

                ((TextView)findViewById(R.id.countdown)).setText(elapsedTime);
            }

            @Override
            public void onFinish() {
                setJerryPos();
            }
        }.start();

        MarkerOptions markerOptions = new MarkerOptions().position(jerryPos).title("Jerry");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry_face));
        mMap.addMarker(markerOptions);
        jerryPos = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Kompas
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

    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskParseJson.java";

        // set your json string url here
        String yourJsonStringUrl = "http://167.205.32.46/pbd/api/track?nim=13512033";

        // contacts JSONArray
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {

            try {

                // instantiate our json parser
                JsonParser jParser = new JsonParser();

                // get json string from url
                JSONObject json = jParser.getJSONFromUrl(yourJsonStringUrl);

                // Storing each json item in variable
                String latitude = json.getString("lat");
                String longitude = json.getString("long");
                String validUntil = json.getString("valid_until");

                jerryPos = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                // show the values in our logcat
                Log.e(TAG, "latitude: " + latitude
                        + ", longitude: " + longitude
                        + ", valid until: " + validUntil);

                // nyimpen validuntil di variabel time
                validTime = Long.parseLong(validUntil);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {}

    }
}
