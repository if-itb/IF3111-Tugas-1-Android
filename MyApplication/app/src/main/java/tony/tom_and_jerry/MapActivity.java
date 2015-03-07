package tony.tom_and_jerry;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MapActivity extends Activity implements SensorEventListener {
    private GoogleMap map;
    private LatLng jerryPosition, tomPosition;
    private TextView jerryCoordinateText, tomCoordinateText,timeLimitText;
    private ImageView compassImage;
    private int duration;
    private boolean showTomPos = false;

    /* Magnet Sensor Variables */
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        new RequestJerryLocation().execute("http://167.205.32.46/pbd/api/track?nim=13512018");

        jerryCoordinateText = (TextView) findViewById(R.id.jerryPosition);
        jerryCoordinateText.setSingleLine(false);

        tomCoordinateText = (TextView) findViewById(R.id.tomPosition);
        tomCoordinateText.setSingleLine(false);

        timeLimitText = (TextView) findViewById(R.id.timeLimit);
        timeLimitText.setSingleLine(false);

        compassImage = (ImageView) findViewById(R.id.compass);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class RequestJerryLocation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out;
                    out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            try {
                JSONObject jObject = new JSONObject(result);
                double latitude = jObject.getDouble("lat");
                double longitude = jObject.getDouble("long");
                int validUntil = jObject.getInt("valid_until");

                long unixTime = System.currentTimeMillis();
                duration = validUntil - (int)unixTime;

                new CountDownTimer(duration, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        int hour, minute, second;
                        hour = ((int)(millisUntilFinished)/1000) / 3600;
                        minute = (((int)millisUntilFinished)/1000 - hour * 3600) / 60;
                        second = ((int)millisUntilFinished/1000 - hour * 3600) % 60;
                        timeLimitText.setText("Searching\nTimeLeft\n" + hour + ":" + minute + ":" + second);
                    }

                    @Override
                    public void onFinish() {
                        new RequestJerryLocation().execute("http://167.205.32.46/pbd/api/track?nim=13512018");
                    }
                }.start();

                /* Get Jerry's Position */
                jerryPosition = new LatLng(latitude,longitude);
                jerryCoordinateText.setText("Jerry's Position\n" + "Lat: " + jerryPosition.latitude + "\nLong: " + jerryPosition.longitude);

                focusOn(jerryPosition, 17.5f);

                if (map!=null) {
                    map.addMarker(new MarkerOptions().position(jerryPosition).title("Jerry is here"));
                    map.setMyLocationEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Do anything with response..
        }
    }

    public void locateTomButton(View view) {

        if (!showTomPos) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            tomPosition = new LatLng(location.getLatitude(), location.getLongitude());

            tomCoordinateText.setText("\nLat: " + tomPosition.latitude + "\nLong: " + tomPosition.longitude);
            showTomPos = true;

            focusOn(tomPosition,17.5f);
        }
        else {
            tomCoordinateText.setText("\n");
            showTomPos = false;
        }
    }

    public void locateJerryButton(View view) {
        if (jerryPosition != null) {
            focusOn(jerryPosition, 19);
        }
    }

    public void qrCodeScanButton(View view) {
        Intent intent = new Intent(this,ScanActivity.class);
        if (intent != null) {
            startActivity(intent);
        }
    }

    public void focusOn(LatLng focusTarget, float zoomSize) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(focusTarget)      // Sets the center of the map to Mountain View
                .zoom(zoomSize)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

            compassImage.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}


