package com.example.bagaskarapramudita.tomjerry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;import android.widget.TextView;
import android.widget.Toast;

public class MapsActivity extends FragmentActivity implements SensorEventListener{

    static final String NIM = "13512073";
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private String mToken;
    private int mKodeServer;
    private String mPesanServer;
    private String mLat;
    private String mLon;
    private String mWaktuJerry;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private SensorManager mSensorManager;
    private ImageView mImage;
    private float mCurrentDegree = 0f;

    public class TaskGet extends AsyncTask<Context, String, String> {

        private Context context;
        private JSONObject isi;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim="+NIM);
            HttpResponse response;
            String result = "";
            try {
                response = client.execute(request);

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                isi = new JSONObject(result);
                mLat = isi.optString("lat");
                mLon = isi.optString("long");

                mWaktuJerry = isi.optString("valid_until");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }
        protected void onPostExecute(String result){
            if (mKodeServer != 0) {
                Toast toast = Toast.makeText(context, "Kode Server: " + mKodeServer + "\nPesan Server: " + mPesanServer, Toast.LENGTH_LONG);
                toast.show();
            }
        }

    }

    public class TaskPost extends AsyncTask<Context, String, String> {

        private Context context;
        private JSONObject isi;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {

            context = params[0];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
            HttpResponse response;
            String result = "";
            try {
                Log.d("NIM", NIM);
                Log.d("TOKEN",mToken);

                isi = new JSONObject();
                isi.put("nim", NIM);
                isi.put("token", mToken);
                Log.d("ISI json", isi.toString());
                StringEntity se = new StringEntity( isi.toString());
                post.setEntity(se);

                post.setHeader("Content-type", "application/json");

                response = client.execute(post);

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result += line;
                }

                isi = new JSONObject(result);
                mKodeServer = isi.optInt("code");
                mPesanServer = isi.optString("message");


                Log.d("KODE", ""+mKodeServer);
                Log.d("ISI KODE", result);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            Toast toast = Toast.makeText(context, "Kode Server: "+ mKodeServer + "\nPesan Server: " + mPesanServer, Toast.LENGTH_LONG);
            toast.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mImage = (ImageView) findViewById(R.id.imageViewCompass);
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }



    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
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
        //Labtek V//
        new TaskGet().execute(getApplicationContext());
        mMap.setMyLocationEnabled(true);
        while(mLat == null || mLon == null || mWaktuJerry == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mLat != null && mLon != null && mWaktuJerry != null){
            mMap.addMarker(new MarkerOptions().position(new LatLng( Double.parseDouble(mLat), Double.parseDouble(mLon) )).title("Jerry"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLon)), 19 ));

            long waktusisa = Long.parseLong(mWaktuJerry)*1000;
            Date date = new Date(waktusisa);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d,yyyy h:mm:ss a", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC+7"));
            String waktuhangus = sdf.format(date);
            TextView time = (TextView) findViewById(R.id.time);
            time.setText("Deadline: \n"+ waktuhangus);
            long waktusekarang = new Date().getTime();
            new CountDownTimer(waktusisa-waktusekarang, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Toast toast = Toast.makeText(getApplicationContext() , "Jerry Berpindah Tempat!!!", Toast.LENGTH_LONG);
                    toast.show();
                    setUpMap();
                }
            }.start();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(mCurrentDegree,-degree,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        // how long the animation will take place
        ra.setDuration(100);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        mImage.startAnimation(ra);
        mCurrentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {

        }
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                mToken = contents;
                new TaskPost().execute(getApplicationContext());
                Toast toast = Toast.makeText(getApplicationContext() , "Result: "+contents+"\nFormat: "+ format, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }


}
