package com.melvin.tubes1pbd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends FragmentActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView compass;
    private Marker previousMarker;
    // record the compass picture angle turned
    private float currentDegree = 0f;

    private ListView listView;
    private boolean lock;
    private ProgressBar bar;
    private Timer UpdateLocation;

     //lat long valid
     private double latitude;
     private double longitude;
     private String valid_until;
     private long durasi;
     //barcode scanner
     static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
     private String result;//untuk result dari HTTPpost
     private boolean lockHTTP=true;
     private boolean destroyed=true;

    private int counter = 0;
    BackgroundTask BackgroundTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        lock = true;
        destroyed = false;
        BackgroundTask = new BackgroundTask();
        BackgroundTask.execute(getApplicationContext());

        while(lock){}
        //mengeluarkan map
        setUpMapIfNeeded();
        //barcode scanner
        Button buttonScanner = (Button) findViewById(R.id.scanner);
        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //start the scanning activity from the com.google.zxing.client.android.SCAN intent
                    Intent intent = new Intent(ACTION_SCAN);
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        //mengeluarkan geomagnetic sensor

        ShowCompass();


    }

    public void ShowCompass ()
    {
        compass = (ImageView) findViewById(R.id.compass);
        //geomagnetic sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        //untuk mengupdate lokasi.
        UpdateLocation = new Timer();
        UpdateLocation.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateMap();
                        setUpMap();
                    }
                });
            }
        }, 0,durasi);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);


    }

    public void onDestroy(){
        destroyed = true;
        BackgroundTask.cancel(true);
        super.onDestroy();
    }

    private void updateMap(){

        new BackgroundTask().execute(getApplicationContext());
    }

    @Override

    protected void onPause() {
        super.onPause();

        UpdateLocation.cancel();
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
                Animation.RELATIVE_TO_SELF, 0.5f);
        // how long the animation will take place

        ra.setDuration(210);
        // set the animation after the end of the reservation status

        ra.setFillAfter(true);
        // Start the animation
        compass.startAnimation(ra);
        currentDegree = -degree;
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

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    //RSS reader
    public class BackgroundTask extends AsyncTask<Context, String, String> {

        private Context context;

        public void parse(String result) {
            String lat = "", lon = "", val = "";
            int len = result.length();
            int i = 0;
            int j = 0;
            while (i<len) {
                if (j==3 && result.charAt(i)!='"') {
                    lat += result.charAt(i);
                }
                else if (j==7 && result.charAt(i)!='"') {
                    lon += result.charAt(i);
                }
                else if (j==10 && result.charAt(i)!=':' && result.charAt(i)!='}') {
                    val += result.charAt(i);
                }
                if (result.charAt(i)=='"') j++;
                i++;
            }

            latitude = Double.valueOf(lat.trim());
            longitude = Double.valueOf(lon.trim());
            valid_until = val.trim();
            Log.d("ResultParsing", "[Parse] "+Double.toString(latitude)+" "+Double.toString(longitude)+" "+valid_until);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            // TODO Auto-generated method stub

           /* DUMMY untuk testing
               counter++;
           if(counter==1)
                result = "{\"lat\":\"-6.890323\", \"long\":\"108\", \"valid_until\":1425545130}";
           else if(counter==2)
                result = "{\"lat\":\"-10\", \"long\":\"107.610382\", \"valid_until\":1425545170}";
           else if(counter==3) {
               result = "{\"lat\":\"-15\", \"long\":\"105.610382\", \"valid_until\":1425545200}";
                counter = 0;
           }*/


            context = params[0];
            listView = (ListView) findViewById(R.id.list);

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512085");
            HttpResponse response;
            //String result = dummy[counter];

                try {
                    response = client.execute(request);

                    // Get the response
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result += line;
                    }
                    String TAG = "tesdoang";
                    Log.d(TAG, result);
                    parse(result);

                    //menghitung epoch untuk mengetahui durasi
                    Date date = new Date();
                    long epoch = date.getTime();
                    long valid_unt = Long.parseLong(valid_until);
                    valid_unt = valid_unt * 1000;
                    durasi = valid_unt - epoch;

                    lock = false;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            /*if (counter<2)
                counter++;
            */
            return result;
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                final String contents = intent.getStringExtra("SCAN_RESULT");
                System.out.println(contents);
                Toast toast = Toast.makeText(this, "Content:" + contents , Toast.LENGTH_LONG);
                toast.show();

                new Thread(new Runnable() {
                    public void run() {
                        //send to server
                        HttpClient client = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost("http://167.205.32.46/pbd/api/catch");
                        httppost.setHeader("Content-type", "application/json");
                        //set parameter request
                        List nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("token", contents));
                        nameValuePairs.add(new BasicNameValuePair("nim", "13512085"));
                        try {
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //set body request
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("nim", "13512085");
                            jsonObject.put("token", contents);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            StringEntity se = new StringEntity(jsonObject.toString());
                            se.setContentType("application/json;charset=UTF-8");
                            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
                            httppost.setEntity(se);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        HttpResponse response;

                        try {
                            response = client.execute(httppost);
                            if (response != null) {
                                if (response.getStatusLine().getStatusCode() == 200)
                                    result = "200 OK";
                                else if (response.getStatusLine().getStatusCode() == 400)
                                    result = "400 missing parameter";
                                else if (response.getStatusLine().getStatusCode() == 403)
                                    result = "403 Forbidden";
                                else
                                    result= response.getStatusLine().getStatusCode()+"";
                            }
                            else
                            {
                                result="null";
                            }
                            lockHTTP=false;

                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            result="error";
                            lockHTTP=false;
                        }
                    }
                }).start();

                while(lockHTTP){}
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }
    }


}
