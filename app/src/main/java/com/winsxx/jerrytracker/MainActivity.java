package com.winsxx.jerrytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private static final String LOCATION_ENDPOINT = "http://167.205.32.46/pbd/api/track";
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final String CATCH_ENDPOINT = "http://167.205.32.46/pbd/api/catch";
    private static final String NIM = "13512071";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private ImageView mPointer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private GoogleMap mGoogleMap;
    private Marker mJerryLocationMarker;

    private Handler mHandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new JerryLocationUpdater().execute();
        }
    };

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
                } catch (ActivityNotFoundException notFoundException) {
                    //Do nothing
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        return downloadDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mPointer = (ImageView) findViewById(R.id.pointer);

        initializedMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean accel = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        boolean magnet = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        initializedMap();
        new JerryLocationUpdater().execute();

        Log.d("Sensor", "Accel " + accel);
        Log.d("Sensor", "Magnet " + magnet);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians + 360)) % 360;

            RotateAnimation ra = new RotateAnimation(mCurrentDegree, -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(250);
            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void scanQR(View view) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException notFoundException) {
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");

                String[] params = new String[3];
                params[0] = CATCH_ENDPOINT;
                params[1] = NIM;
                params[2] = contents;
                new SubmitCatchToken().execute(params);
            }
        }
    }

    public String sendCatchToken(String endpoint, String nim, String token) {
        JSONObject catchCode = new JSONObject();
        try {
            Log.d("catch", "start making json object");

            catchCode.put("nim", nim);
            catchCode.put("token", token);
            Log.d("catch", "done making json object");

            HttpPost httpPost = new HttpPost(endpoint);
            Log.d("catch", catchCode.toString());
            httpPost.setEntity(new StringEntity(catchCode.toString()));
            httpPost.setHeader("Content-type", "application/json");

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                return "Success to catch Jerry";
            } else if (responseCode == 403) {
                return "Catch wrong one";
            } else {
                return "Technical issue in catching";
            }

        } catch (JSONException ex) {
            return "Catcher Machine Error";
        } catch (UnsupportedEncodingException encex) {
            return "Catcher Machine Error";
        } catch (ClientProtocolException pex) {
            return "Send request fail";
        } catch (IOException ioex) {
            return "Send request fail";
        }
    }

    private void initializedMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            LatLng jerryLocation;
            jerryLocation = new LatLng(0, 0);
            mJerryLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(jerryLocation).title("Jerry Location"));
            mGoogleMap.setMyLocationEnabled(true);

            if (mGoogleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void reloadJerryLocation(View view) {
        new JerryLocationUpdater().execute();
    }

    private class SubmitCatchToken extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d("catch", "background process called");
            return sendCatchToken(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

    }

    private class JerryLocationUpdater extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(LOCATION_ENDPOINT + "?nim=" + NIM);
                Log.d("location", "Endpoint = " + httpGet.getURI());

                HttpResponse response = httpClient.execute(httpGet);
                Log.d("location", "Status code: " + response.getStatusLine().getStatusCode());

                HttpEntity httpEntity = response.getEntity();

                String jsonString = EntityUtils.toString(httpEntity, "utf-8");

                Log.d("location", "JsonString: " + jsonString);
                return new JSONObject(jsonString);
            } catch (IOException ioex) {
                Log.d("location", "IOException");
                return null;
            } catch (JSONException jex) {
                Log.d("location", "JsonException");
                return null;
            }

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {

                try {
                    long validUntil;
                    double lat = jsonObject.getDouble("lat");
                    double lng = jsonObject.getDouble("long");
                    validUntil = jsonObject.getLong("valid_until");

                    LatLng jerryLocation = new LatLng(lat, lng);
                    mJerryLocationMarker.setPosition(jerryLocation);
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jerryLocation, 15));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);


                    Log.d("location", "Current time: " + System.currentTimeMillis());
                    long delay = validUntil * 1000 - System.currentTimeMillis();
                    if (delay >= 0) {
                        mHandler.removeCallbacks(runnable);
                        mHandler.postDelayed(runnable, delay);
                        Toast.makeText(getApplicationContext(), "New location valid for " + delay / 1000 / 60 + " minutes", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException jex) {
                    Toast.makeText(getApplicationContext(), "Fail to Update Location", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Couldn't get Jerry location from server", Toast.LENGTH_LONG).show();
            }
        }
    }


}
