package com.yusufrahmatullah.letscatchjerry;

import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements SensorEventListener{

    //Konstanta untuk QRCode menggunakan QRDroid
    private static final String ACTION_SCAN = "la.droid.qr.scan";
    private static final String ACTION_ENCODE = "la.droid.qr.encode";
    private static final String OPTION_PARAM = "la.droid.qr.complete";
    private static final String RESULT = "la.droid.qr.result";

    //konstanta untuk get dan post ke server
    private static final String getUrl = "http://167.205.32.46/pbd/api/track?nim=13512040";
    private static final String postUrl = "http://167.205.32.46/pbd/api/catch";

    //gambar kompas
    private ImageView image;
    private float currentDegree = 0f;
    TextView tvHeading;

    //waktu tersisa
    private TextView sisaWaktu;

    //SensorManager untuk sensor Orientation
    private SensorManager sensorManager;

    //Fragment Peta
    private final MapsFragment mapsFragment = new MapsFragment();

    //this context
    private final Context mainContext = this;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //gambar untuk kompas
        image = (ImageView)findViewById(R.id.compassImage);
        //tulisan derajat pada compass
        tvHeading = (TextView)findViewById(R.id.tvHeading);

        //inisialisasi sisaWaktu
        sisaWaktu = (TextView)findViewById(R.id.sisaWaktu);

        //inisialisasi sensor
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //inisialisasi posisi Jerry
        getJerryLocation();

        //inisialisasi peta
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mapView, mapsFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    /**
     * mendapatkan lokasi Jerry dari server
     */
    private void getJerryLocation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        double lat=0, lng=0;
                        long time=0;

                        //mendapatkan posisi Jerry dari server beserta kadaluarsa
                        //parsing JSON menggunakna Google JSON
                        Gson gson = new Gson();
                        response = response.substring(response.indexOf('{'), response.lastIndexOf('}')+1);
                        String json = gson.toJson(response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            lat = jsonObject.getDouble("lat");
                            lng = jsonObject.getDouble("long");
                            //mendapatkan waktu dari server dengan menambahkan 7 jam (UTC+7)
                            time = jsonObject.getLong("valid_until") + 7 * 3600 * 1000;

                            mapsFragment.setJerryPosition(lat, lng);

                            Date now = new Date();
                            long nowTime = now.getTime();

                            //melakukan update posisi Jerry berdasarkan kadaluarsa
                            new CountDownTimer(time*1000 - nowTime, 1000){

                                @Override
                                public void onTick(long millisUntilFinished) {
                                    Date d = new Date(millisUntilFinished);
                                    sisaWaktu.setText("Time left : "+d.getHours() + " h "+d.getMinutes()+" m "+d.getSeconds()+" s");
                                }

                                @Override
                                public void onFinish() {
                                    getJerryLocation();
                                }
                            }.start();
                        } catch (JSONException e) {
                            Toast.makeText(mainContext, "Parsing JSON failed", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mainContext, "Response error", Toast.LENGTH_LONG).show();
                    }
        });
        queue.add(stringRequest);
    }

    /**
     * menangkap Jerry dengan mengirim token ke server
     * @param token
     */
    private void captureJerry(final String token){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int code;
                        String message;

                        //mengirimkan token bahwa Jerry tertangkap
                        Gson gson = new Gson();
                        response = response.substring(response.indexOf('{'), response.lastIndexOf('}')+1);
                        String json = gson.toJson(response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            code = jsonObject.getInt("code");
                            message = jsonObject.getString("message");
                            if(code==200){
                                Toast.makeText(mainContext, "Jerry captured", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(mainContext, "Capture Jerry failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(mainContext,json, Toast.LENGTH_LONG).show();
                            Toast.makeText(mainContext, "Parsing JSON after post failed", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mainContext, "Response error : "+error, Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nim", "13512040");
                params.put("token", token);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    /**
     * melakukan scan QRCode menggunakan QRDroid
     * @param v
     */
    public void scanQR(View v){
        try{
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra(OPTION_PARAM, true);
            startActivityForResult(intent, 0);
        }catch(ActivityNotFoundException afne){
            Toast.makeText(this, "Please Install QRDroid", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * mengirim hasil pembacaan QRCode ke server
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK){
            String token = data.getStringExtra(RESULT);
            captureJerry(token);
        }
    }

    /**
     * setiap ada peubahan pada sensor orientation, gambar kompas diputar
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
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
}
