package com.example.andrey.jerrytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements SensorEventListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker jerrymarker = null;
    private double jlatitude;
    private double jlongitude;
    private double clatitude;
    private double clongitude;
    private LatLng jerrylocation = null;
    private LatLng currentposition = null;
    private String contents = "";
    private long validuntil;
    private long currenttime = System.currentTimeMillis();

//    private LocationClient mLocationClient;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getRequest();
//        trackJerryAgain();

        //compass
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener((android.hardware.SensorEventListener) this);
    }

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

    private void setUpMap() {
        jerrymarker = mMap.addMarker(new MarkerOptions()
                    .position(jerrylocation)
                    .title("Jerry is here!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerrymarker)));
        mMap.setMyLocationEnabled(true);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = supportMapFragment.getMap();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null){
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider,20000,0,this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentposition, 15));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compass, menu);
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
        image.startAnimation(ra);
        currentDegree = -degree;
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
            //on catch, show the download dialog
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
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
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = intent.getStringExtra("SCAN_RESULT");
                Toast toast = Toast.makeText(this, "Content:" + contents, Toast.LENGTH_LONG);
                toast.show();
                sendToSpike();
            }
        }
    }

    public void getRequest() {
        RequestTask RT = new RequestTask();
        RT.execute("http://167.205.32.46/pbd/api/track?nim=13512058");
    }

    public void sendToSpike(){
        ServerSender SS = new ServerSender();
        SS.execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
        clatitude = location.getLatitude();
        clongitude = location.getLongitude();
        currentposition = new LatLng(clatitude, clongitude);
        float distancetojerry = getDistance(clatitude,clongitude,jlatitude,jlongitude);
        locationTv.setText("Distance to Jerry is now: "+distancetojerry+" meters");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class RequestTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... uri) {
            HttpGet httpRequest = new HttpGet(URI.create(uri[0]));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = null;
            String content = "";
            JSONObject json = null;
            try {
                response = (HttpResponse) httpclient.execute(httpRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufHttpEntity = null;
            try {
                bufHttpEntity = new BufferedHttpEntity(entity);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        bufHttpEntity.getContent()));
                StringBuilder sb = new StringBuilder();
                String line = "";

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "");
                }

                // Append Server Response To Content String
                content = sb.toString();
                try {
                    json = new JSONObject(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Get the server response
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                jlatitude = Double.parseDouble(result.getString("lat"));
                jlongitude = Double.parseDouble(result.getString("long"));
                validuntil = Long.parseLong(result.getString("valid_until"));
                jerrylocation = new LatLng(jlatitude,jlongitude);
                Toast toastmap = Toast.makeText(MapsActivity.this,""+jlatitude+","+jlongitude,Toast.LENGTH_LONG);
                toastmap.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpMapIfNeeded();
        }
    }

    private class ServerSender extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... uri) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            HttpResponse response;
            JSONObject json = new JSONObject();
            String content = "";

            try {
                HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                json.put("nim", "13512058");
                json.put("token", contents);
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);

                if (response != null) {
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity buffHttpEntity = new BufferedHttpEntity(entity);
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(
                            buffHttpEntity.getContent()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "");
                    }
                    content = sb.toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast toastreply = Toast.makeText(MapsActivity.this, result, Toast.LENGTH_LONG);
            toastreply.show();
            setUpMapIfNeeded();
        }
    }

    private void trackJerryAgain(){
        new CountDownTimer(validuntil*1000 - currenttime, 1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try {
                    JSONObject json = new RequestTask().execute().get();
                    if(json!=null){
                        jlatitude = Double.parseDouble(json.getString("lat"));
                        jlongitude = Double.parseDouble(json.getString("long"));
                        validuntil = Long.parseLong(json.getString("valid_until"));
                        jerrymarker.remove();

                        setUpMap();
                        trackJerryAgain();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private float getDistance(double lat1, double lon1, double lat2, double lon2){
        float distance = 0;
        int R = 6371000;
        double tetta1 = Math.toRadians(lat1);
        double tetta2 = Math.toRadians(lat2);
        double dtetta = Math.toRadians(lat2-lat1);
        double dlambda = Math.toRadians(lon2-lon1);

        double a = Math.sin(dtetta/2) * Math.sin(dtetta/2) +
                   Math.cos(tetta1) * Math.cos(tetta2) *
                   Math.sin(dlambda/2) * Math.sin(dlambda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        distance = (float) d;

        return distance;
    }
}