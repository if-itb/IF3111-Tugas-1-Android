package eld.tracker;

import android.app.Dialog;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

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
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

public class MapsActivity extends FragmentActivity implements SensorEventListener, LocationListener{
    // Map Attributes
    private LatLng tes = new LatLng(-5.890323,107.610381);
    private LatLng JerryPosition = tes;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private long time = 1;
    private TextView text;
    private String contents = "";

    // Compass Attributes
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    // Jerry Image
    private ImageView jerry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // My Location
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if(status!= ConnectionResult.SUCCESS){ // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }
        else { // Google Play Services are available
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting GoogleMap object from the fragment
            mMap = fm.getMap();
            // Enabling MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null){
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);

            // Setting TextView for Time Left
            text = (TextView) findViewById(R.id.textView);
            text.setText("Time Left : " + String.valueOf(time) + " second");

            // Getting Request from Server
            getRequest();

            // Setting ImageView for Jerry
            jerry = (ImageView) findViewById(R.id.imageViewJerry);
            jerry.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(JerryPosition)      // Sets the center of the map to LatLng (refer to previous snippet)
                                    .zoom(17)                   // Sets the zoom
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
            );

            // Setting ImageView for Compass
            image = (ImageView) findViewById(R.id.imageViewCompass);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            // Setting Map
            setUpMapIfNeeded();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView tvLocation = (TextView) findViewById(R.id.tv_location);
        // Getting latitude of the current location
        double latitude = location.getLatitude();
        // Getting longitude of the current location
        double longitude = location.getLongitude();
        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        // Setting latitude and longitude in the TextView tv_location
        tvLocation.setText("Latitude:" +  latitude  + ", Longitude:"+ longitude );
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
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
        if(time == 0){
            getRequest();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(JerryPosition).title("Catch Me if You Can").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry1)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JerryPosition, 17));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        RotateAnimation ra = new RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree + 90;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    // QRCode
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

    // Alert Dialog for Downloading
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
                }
                catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    // Sending Token to Server after Scanning
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content: " + contents + " Format: " + format, Toast.LENGTH_LONG);
                toast.show();
                postRequest();
            }
        }
    }

    // Asking Spike about Jerry Position
    public void getRequest(){
        Toast toast = Toast.makeText(MapsActivity.this,"Getting new Request from server",Toast.LENGTH_LONG);
        toast.show();
        GetTask GT = new GetTask();
        GT.execute("http://167.205.32.46/pbd/api/track?nim=13512002");
    }

    private class GetTask extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... uri) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(uri[0]));
            HttpResponse response = null;
            JSONObject json = null;
            try {
                response = client.execute(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity entity = response.getEntity();
            try{
                BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = rd.readLine()) != null){
                    sb.append(line);
                }
                try{
                    json = new JSONObject(sb.toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try{
                double lat = Double.parseDouble(result.getString("lat"));
                double lng = Double.parseDouble(result.getString("long"));
                time = Long.parseLong(result.getString("valid_until")) * 1000;
                JerryPosition = new LatLng(lat,lng);
                new CountDownTimer((time - System.currentTimeMillis()), 1000) {
                    public void onTick(long millisUntilFinished) {
                        long x = (time-System.currentTimeMillis())/1000;
                        text.setText("Time Left : " +
                                String.valueOf(x/3600) + " hour(s) " +
                                String.valueOf(x % 3600/60) + " minute(s) " +
                                String.valueOf((x % 3600)%60) + " second(s)");
                    }
                    public void onFinish() {
                        time = 0;
                        setUpMap();
                    }
                }.start();
                Toast toast = Toast.makeText(MapsActivity.this,"Jerry Position:("+lat+" , "+lng+")",Toast.LENGTH_LONG);
                toast.show();
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            setUpMap();
        }
    }

    // Telling Spike about Jerry Catching
    public void postRequest(){
        PostTask PT = new PostTask();
        PT.execute();
    }

    private class PostTask extends AsyncTask<Void, String, String>{
        @Override
        protected String doInBackground(Void... uri){
            HttpClient client = new DefaultHttpClient();
            JSONObject json = new JSONObject();
            String content = "";
            try{
                HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                json.put("nim", "13512002");
                json.put("token", contents);
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                post.setEntity(se);
                HttpResponse response = client.execute(post);
                if(response != null){
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    content = sb.toString();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return content;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Toast toast = Toast.makeText(MapsActivity.this,result,Toast.LENGTH_LONG);
            toast.show();
        }
    }
}