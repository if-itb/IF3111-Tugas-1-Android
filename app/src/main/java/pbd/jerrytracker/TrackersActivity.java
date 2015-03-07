package pbd.jerrytracker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static pbd.jerrytracker.R.*;

public class TrackersActivity extends FragmentActivity implements SensorEventListener {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    // define the display assembly compass picture
    private ImageView image;

    // device sensor manager
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
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double latitude;
    private double longitude;
    private long validuntil_epoch;
    private Date locationends;
    private SimpleDateFormat simplelocationends;
    private long timeremains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        latitude = 0;
        longitude = 0;
        setContentView(layout.activity_trackers);
        setUpMapIfNeeded();
        RefreshLocation();
        //getLocation.execute("http://167.205.32.46/pbd/api/track?nim=13512063");
        image = (ImageView) findViewById(id.imageView);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void RefreshLocation(){
        AsyncTask<String,String,String> newLocation = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... uri) {
                Log.d("getLocation", "refreshed");
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;
                String responseString = null;
                try {
                    response = httpclient.execute(new HttpGet(uri[0]));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        responseString = out.toString();
                        out.close();
                    } else {
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return responseString;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                try{
                    Log.d("TrackersActivity","result:" + result);
                    JSONObject json = new JSONObject(result);
                    String latitude_str = json.getString("lat");
                    String longitude_str = json.getString("long");
                    String valid_str = json.getString("valid_until");
                    latitude = Double.valueOf(latitude_str);
                    longitude = Double.valueOf(longitude_str);
                    validuntil_epoch = Integer.valueOf(valid_str);
                    long current_time = System.currentTimeMillis()/1000;
                    locationends = new Date(validuntil_epoch*1000);
                    timeremains = validuntil_epoch - current_time;
                    Log.d("valid_until in strings",valid_str);
                    Log.d("valid until in long",String.valueOf(validuntil_epoch));
                    Log.d("location ends",locationends.toString());
                    Log.d("time remaining",String.valueOf(timeremains));
                    //timeremains = 10;
                    setUpMap();
                    new CountDownTimer(timeremains*1000,1000){
                        public void onTick(long secondsleft) {
                            TextView timeview = (TextView) findViewById(id.textView);
                            timeview.setText("Jerry will move in: "+ secondsleft/1000 + "\n(" + locationends.toString() + ")");
                            Log.d("time remains", String.valueOf(secondsleft));
                        }
                        public void onFinish(){
                            RefreshLocation();
                        }
                    }.start();
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        newLocation.execute("http://167.205.32.46/pbd/api/track?nim=13512063");
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
            Toast.makeText(TrackersActivity.this,"No Scanner Found",Toast.LENGTH_LONG).show();
            //showDialog(TrackersActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    //on ActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String token="";
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                token = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                //Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                //toast.show();
            }
        }

        AsyncTask<String,String,String> PostRequest = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                //Log.d("2ndparams",params[1]);
                String responseText = "";
                try {

                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(params[0]);

                    //String json = "";

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("nim", "13512063");
                    jsonObject.put("token", params[1]);

                    StringEntity se = new StringEntity(jsonObject.toString());

                    httpPost.setEntity(se);

                    HttpResponse httpresponse = httpclient.execute(httpPost);

                    responseText = EntityUtils.toString(httpresponse.getEntity());

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("Parse Exception", e + "");
                }
                return responseText;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.d("Tracker.catch", "postexecuted. " + result);
                result = result.substring(result.indexOf("{"));
                int code;
                try{
                    JSONObject resultjson = new JSONObject(result);
                    code = Integer.valueOf(resultjson.getString("code"));
                    Toast toast = Toast.makeText(TrackersActivity.this, result, Toast.LENGTH_LONG);
                    if(code==200)
                    {
                        toast = Toast.makeText(TrackersActivity.this, "You found Jerry!", Toast.LENGTH_LONG);
                    }
                    else if(code==400){
                        toast = Toast.makeText(TrackersActivity.this, "Code 400: Missing parameters", Toast.LENGTH_LONG);
                    }
                    else{
                        toast = Toast.makeText(TrackersActivity.this, "Code 403: Forbidden, wrong parameter", Toast.LENGTH_LONG);
                    }
                    toast.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        PostRequest.execute("http://167.205.32.46/pbd/api/catch",token);
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
            //mMap = (GoogleMap) R.id.map;
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("TrackersActivity","sensor change");
        if (event.sensor == mAccelerometer) {
            //Log.d("TrackersActivity","accelerometer changed");
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        if (event.sensor == mMagnetometer) {
            //Log.d("TrackersActivity","magnetometer changed");
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        //image.startAnimation(new RotateAnimation(30f,180f));
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            //Log.d("TrackersActivity","masuk if");
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

            image.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        Log.d("TrackersActivity","map set");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.898198, 107.612409),13));
        if(latitude!=0&&longitude!=0)
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry Location"));
    }
}
