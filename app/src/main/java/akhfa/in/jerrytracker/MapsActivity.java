package akhfa.in.jerrytracker;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MapsActivity extends FragmentActivity implements SensorEventListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    Marker JerryPos;

    //Buat json
    private static final String TAG_LATITUDE = "lat";
    private static final String TAG_LONGITUDE = "long";
    private static final String TAG_VALID = "valid_until";
    private static final String url= "http://167.205.32.46/pbd/api/track?nim=13513601";
    static boolean jsonBool=false;

    //Buat Compass
    // define the display assembly compass picture
    private ImageView image;
    private TextView timerTextView;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;


    //Buat timer
    private long time;
    private long timeDiff;
    private String valid_until = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        // compass image
        image = (ImageView) findViewById(R.id.compassImage);

        timerTextView = (TextView) findViewById(R.id.timerTextView);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
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
        // not in use
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsAkhfa))
                    .getMap();
            if (mMap != null) {
                //execute parsing json from server
                mMap.setMyLocationEnabled(true);
                new JSONParse().execute();
            }
        }
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONParser jParser = new JSONParser();

            JSONObject json = jParser.getJSONFromUrl(url);
            if(json==null)
            {
                jsonBool=false;
            }
            else jsonBool=true;
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            if(jsonBool==true)
            {
                try{
                    Log.e("status",jsonBool+"");
                    //simpan di variable
                    String latitude = json.getString(TAG_LATITUDE);
                    String longitude = json.getString(TAG_LONGITUDE);
                    valid_until = json.getString(TAG_VALID);

                    Log.e("lat",latitude);
                    Log.e("lat",longitude);
                    Log.e("time", valid_until);

                    Double lat=Double.parseDouble(latitude.toString());
                    Double longi=Double.parseDouble(longitude.toString());

                    //change marker posisition
                    LatLng posisi = new LatLng(lat, longi);
                    JerryPos = mMap.addMarker(new MarkerOptions().position(posisi).title("location").snippet(""));

                    //Update camera to point the marker
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi), 18.0f));
                    countDownStart();

                }catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
            //Jika tidak ada koneksi atau server down, keluarkan message error
            else {Toast.makeText(getApplicationContext(), "error getting data", Toast.LENGTH_SHORT).show();
                Log.e("status",jsonBool+"");}
        }

    }

    public void countDownStart(){
        time = Long.valueOf(valid_until)*1000;
        timeDiff = time - System.currentTimeMillis();
        new CountDownTimer(timeDiff, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                long cur = (time - System.currentTimeMillis())/1000;
                Log.e("different", "" + cur);
                timerTextView.setText(cur/3600+":"+cur%3600/60+":"+cur%3600%60);
                // validTextView.setText("Time before Jerry run = "+valid_until);
                        }
            @Override
            public void onFinish() {
                JerryPos.remove();
                new JSONParse().execute();
                timerTextView.setText("0");
                }
            }.start();
        }
}

