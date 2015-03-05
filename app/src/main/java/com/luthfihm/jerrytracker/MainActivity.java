package com.luthfihm.jerrytracker;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {
    private static SensorManager sensorService;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private Sensor sensor;
    private ImageView compass;
    private TextView textView;
    private float currentDegree;
    private float zoom;
    private GoogleMap mMap;
    private Marker currentLocation;
    private Marker jerryLocation;
    private Jerry jerry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compass = (ImageView) findViewById(R.id.compass);
        currentDegree = 0f;
        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        textView = (TextView) findViewById(R.id.textView);
        jerry = new Jerry();
        if (isConnected()) {
            Toast.makeText(this, "connected!",
                    Toast.LENGTH_LONG).show();
            new UpdateJerryTask().execute("http://167.205.32.46/pbd/api/track?nim=13512100");
        }
        else
            Toast.makeText(this, "not connected!",
                    Toast.LENGTH_LONG).show();
        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        zoom = 18;

        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoom += 1;
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoom += -1;
            }
        });
        setUpMap();
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float azimuth = event.values[0];
            currentDegree = -azimuth;
            compass.setRotation(currentDegree);
            updateCamera(azimuth);
        }
    };


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

    public void setUpMap()
    {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        currentLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.tom)));
        jerryLocation = mMap.addMarker(new MarkerOptions().position(jerry.getLatLng()).title("Jerry's here!").snippet("Click to catch!").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)));
        jerryLocation.showInfoWindow();
        updateCamera(0);
    }

    public void updateCamera(float bearing) {
        GPSTracker gps = new GPSTracker(this);
        jerryLocation.setPosition(jerry.getLatLng());
        if (gps.canGetLocation)
        {
            currentLocation.setPosition(new LatLng(gps.getLatitude(),gps.getLongitude()));
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(gps.getLatitude(), gps.getLongitude()))
                    .bearing(bearing).tilt(30f).zoom(zoom).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
        }

    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.e("Test","Gagal!");
        }

        return result;
    }
    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getPosition().equals(jerryLocation.getPosition()))
        {
            scanQR();
            jerryLocation.showInfoWindow();
        }
        return true;
    }

    private class UpdateJerryTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            // replace with your url

            HttpResponse response;
            try {
                response = client.execute(request);
                return convertInputStreamToString(response.getEntity().getContent());
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            jerry.update(result);
            textView.setText("You have time until :\n"+jerry.getTime()+"\nLet's catch Jerry Tom!");
            Toast.makeText(getBaseContext(), "Jerry's info was updated!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private class CatchJerryTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {

            HttpClient httpClient = new DefaultHttpClient();
            // replace with your url
            HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");


            //Post Data
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("nim", data[0]));
            nameValuePair.add(new BasicNameValuePair("token", data[1]));


            //Encoding POST data
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {

            }

            //making POST request.
            try {
                HttpResponse response = httpClient.execute(httpPost);
                return convertInputStreamToString(response.getEntity().getContent());
            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(getBaseContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void scanQR() {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog

        }
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String token = intent.getStringExtra("SCAN_RESULT");
                Toast toast = Toast.makeText(this, "Sending data...", Toast.LENGTH_LONG);
                toast.show();
                new CatchJerryTask().execute("13512100", token);
            }
        }
    }
}
