package wiragotama.tomandjerryv3;

/**
 * Created by wira gotama on 3/1/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    /* Google maps */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double latitude;
    private double longitude;
    private String valid_until;
    private boolean destroyed;
    private Marker previousMarker; //previous Marker on googleMap

    /* For compass */
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
    private float mCurrentDegree = 0.0f;

    /* Button */
    private Button button;

    /* Time */
    private long epoch;

    /* Background */
    Task backgroundtask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        destroyed = false;
        setUpMapIfNeeded();
        backgroundtask = new Task();
        backgroundtask.execute(getApplicationContext());
        setUpCompass();
        setUpButton();

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    /*
        instantiate map
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMarker();
            }
        }
    }

    /*
        set marker in map at latitude and longitude Jerry placed
     */
    private void setUpMarker() {
        Log.d("map", "[MAP] " + Double.toString(latitude) + " " + Double.toString(longitude));
        if (previousMarker!=null)
                previousMarker.remove();

        previousMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        previousMarker.showInfoWindow();
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.setMyLocationEnabled(true);
    }

    /*
        instantiate compass
     */
    private void setUpCompass() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.pointer);
    }

    /*
        instantiate button to invoke QR Scanner
     */
    private void setUpButton() {
        button = (Button) findViewById(R.id.QR_scan);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MapsActivity.this);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Toast.makeText(getApplicationContext(), scanResult.getContents(), Toast.LENGTH_SHORT).show();
            Log.d("scan result", "[SCAN] "+scanResult.getContents());
            try {
                    Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendPost(scanResult.getContents());
                        } catch (Exception e) {
                            Log.d("[POST]", "[POST] thread Exception");
                        }
                    }
                });
                thread.start();
            } catch (Exception e) {
                Log.d("cannot send post", "[POST] cannot make post request");
            }
        }
    }

    /*
        Http Post reqeust to catch Jerry
     */
    private void sendPost(String token) throws Exception  {
        JSONObject json = new JSONObject();
        json.put("nim", "13512015");
        json.put("token", token);

        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch");
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            Log.d("[POST]", "[POST] send post with token"+json.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String result = rd.readLine();
            JSONObject o = new JSONObject(result);
            Log.d("[POST]", "[POST] Response "+o.toString());
            if (o.getInt("code")==200) {
                Log.d("[POST]", "[POST] TOKEN OK");
            }
            else if (o.getInt("code")==400) {
                Log.d("[POST]", "[POST] Missing Parameter");
            }
            else if (o.getInt("code")==403) {
                Log.d("[POST]", "[POST] Forbidden");
            }
        } catch (Exception ex) {
            Log.d("[POST]", "[POST] send post caught exception");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        backgroundtask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

            ra.setDuration(300);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
        Class that handle asynchronus Http Get Request to get newest information where Jerry is located
        this class also update map (to the newest Jerry's location)
     */
    public class Task extends AsyncTask<Context, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            //for testing only
            /*String dummy[] = new String[3];
            dummy[0] = "{\"lat\":\"-8\", \"long\":\"108\", \"valid_until\":1425545130}";
            dummy[1] = "{\"lat\":\"-8\", \"long\":\"-2\", \"valid_until\":1425545170}";
            dummy[2] = "{\"lat\":\"10\", \"long\":\"10\", \"valid_until\":1425545200}";
            int counter = 0;*/

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512015");
            HttpResponse response;
            String result = "";

            while (!destroyed) {
                Log.d("[GET]", "[GET] start");
                try {
                    response = client.execute(request);

                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    result = rd.readLine();
                    Log.d("Http result", "[GET] " + result);

                    //for testing only
                    //result = dummy[counter%3];

                    parse(result);
                    epoch = Long.valueOf(valid_until)-(System.currentTimeMillis()/1000) - 1;
                    Log.d("epoch", String.valueOf(Long.valueOf(valid_until) - (System.currentTimeMillis() / 1000)));

                    if (epoch>0) {
                        /* Set marker about newest Jerry location in map */
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("map", "[MAP] " + Double.toString(latitude) + " " + Double.toString(longitude));

                                        if (previousMarker != null)
                                            previousMarker.remove();

                                        previousMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                        previousMarker.showInfoWindow();
                                        mMap.getUiSettings().setMapToolbarEnabled(true);
                                        mMap.setMyLocationEnabled(true);

                                        View mapView = (View) findViewById(R.id.map);
                                        mapView.invalidate();
                                        mapView.forceLayout();
                                        Toast.makeText(getApplicationContext(), "New Jerry Location!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        };
                        thread.start();
                    }

                } catch (Exception e) {
                    Log.d("fail", "[GET] Http Get Request Fail "+e.toString());
                }
                if (epoch>0) {
                    Log.d("sleep", "[GET] background sleeps");
                    try {
                        /* sleep thread when not needed */
                        Thread.sleep(epoch * 1000);
                    } catch (InterruptedException e) {
                        Log.d("stacktrace", "Stack Trace "+e.toString());
                    }
                }
                //for testing only
                /*if (counter<2)
                counter++;*/
            }
            return String.valueOf("true");
        }

        /*
            Parse response (JSON object) into it's components
         */
        public void parse(String result) {
            String lat = "", lon = "", val = "";
            int len = result.length();
            int i = 0;
            int count = 0;
            while (i<len) {
                if (count==3 && result.charAt(i)!='"') {
                    lat += result.charAt(i);
                }
                else if (count==7 && result.charAt(i)!='"') {
                    lon += result.charAt(i);
                }
                else if (count==10 && result.charAt(i)!=':' && result.charAt(i)!='}') {
                    val += result.charAt(i);
                }
                if (result.charAt(i)=='"') count++;
                i++;
            }

            latitude = Double.valueOf(lat.trim());
            longitude = Double.valueOf(lon.trim());
            valid_until = val.trim();
            Log.d("hasilparsing", "[Parse] "+Double.toString(latitude)+" "+Double.toString(longitude)+" "+valid_until);
        }
    }
}
