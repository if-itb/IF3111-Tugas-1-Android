package localhost.mousecatcher;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.Request;
import util.Response;

public class MapsActivity extends FragmentActivity implements SensorEventListener{

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
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TextView notifTextView;
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        public void run() {
            new RetrieveFeedTask().execute((Void)null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        handler.post(r);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.pointer);
        notifTextView = (TextView) findViewById(R.id.textView2);
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
                mMap.setMyLocationEnabled(true);
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void scanQR (View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.d("", contents +" " + format);
                //Encoding POST data
                class OneShotTask implements Runnable {
                    String str;
                    OneShotTask(String s) { str = s; }
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");
                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                        nameValuePair.add(new BasicNameValuePair("nim", "13512066"));
                        nameValuePair.add(new BasicNameValuePair("token", str));
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                        } catch (UnsupportedEncodingException e)
                        {
                            e.printStackTrace();
                        }
                        try {
                            HttpResponse response = httpClient.execute(httpPost);
                            // write response to log
                            final String s = "Http Post Response:" + String.valueOf(response.getStatusLine().getStatusCode());
                            Log.d("Http Post Response:", String.valueOf(response.getStatusLine().getStatusCode()));
                            notifTextView.post( new Runnable() {
                                @Override
                                public void run() {
                                    notifTextView.setText(s);
                                    Log.d("View:","Setting notification");
                                }
                            });
                        } catch (ClientProtocolException e) {
                            // Log exception
                            e.printStackTrace();
                        } catch (IOException e) {
                            // Log exception
                            e.printStackTrace();
                        }
                    }
                }
                new Thread(new OneShotTask(contents)).start();
            }
        }
    }

class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;
        private double lat;
        private double lon;
        private long time;

        protected String doInBackground(Void... v) {
            URL url = null;
            try {
                url = new URL("http://167.205.32.46/pbd/api/track?nim=13512066");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL Mismatch");
            }
            // Get the response (here the current thread will block until response is returned).
            try {
                BufferedReader in;
                InputStreamReader ir = new InputStreamReader(url.openStream());
                in = new BufferedReader(ir);

                String inputLine = in.readLine();
                JSONObject obj = new JSONObject(inputLine);

                lat = obj.getDouble("lat");
                lon = obj.getDouble("long");
                time = obj.getLong("valid_until");
                //mMap.addMarker(new MarkerOptions()
                //                    .position(new LatLng(obj.getDouble("lat"), obj.getDouble("long")))
                //                    .title("Jerry Position"));
                // TODO use validUntil property
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Response acquring interupted");
            }
            return null;
        }

        protected void onPostExecute(String s) {
            // TODO: check this.exception
            // TODO: do something with the feed
            mMap.clear();
            Marker m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title("Jerry Position"));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(m.getPosition());
            if (mMap.getMyLocation() != null) builder.include(new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude()));
            Log.d("Location : ",lat + " "+ lon);
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
            handler.postDelayed(r,time - System.currentTimeMillis());
        }
    }
}
