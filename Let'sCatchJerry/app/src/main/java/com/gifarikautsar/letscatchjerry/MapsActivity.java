package com.gifarikautsar.letscatchjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import java.net.URI;

public class MapsActivity extends FragmentActivity implements LocationListener, SensorEventListener {

    GoogleMap googleMap;
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private LatLng Jerry;
    private TextView textCounter;
    private long time = 0;
    private String QRContent = "";
    private ImageView imageJerry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Compass
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Jerry Location
        imageJerry = (ImageView) findViewById(R.id.imageJerry);
        imageJerry.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        setUpMap();
                    }
                }
        );

        //Maps
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if(status!= ConnectionResult.SUCCESS){
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            googleMap = fm.getMap();
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null){
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
        }
        getRequest();
        textCounter = (TextView) findViewById(R.id.textCounter);
        textCounter.setText(String.valueOf(time));

    }

    @Override
    protected void onResume() {
        super.onResume();
        googleMap.setMyLocationEnabled(true);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleMap.setMyLocationEnabled(false);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(20)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
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
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //QR Code
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
                //get the extras that are returned from the intent
                QRContent = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + QRContent + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
                postRequest();
            }
        }
    }

    private void setUpMap() {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(Jerry).title("Catch me! :p").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Jerry));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Jerry)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(20)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void getRequest(){
        GetTask GT = new GetTask();
        GT.execute("http://167.205.32.46/pbd/api/track?nim=13512020");
    }

    private class GetTask extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... uri) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(uri[0]));
            HttpEntity entity = null;
            JSONObject json = null;
            StringBuffer sb = new StringBuffer();
            String line = "";
            try{
                HttpResponse response = (HttpResponse) client.execute(request);
                entity = response.getEntity();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try{
                BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
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
            String contents = json.toString();
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            double lat;
            double lng;
            try{
                lat = Double.parseDouble(result.getString("lat"));
                lng = Double.parseDouble(result.getString("long"));
                time = Long.parseLong(result.getString("valid_until")) * 1000;
                Jerry = new LatLng(lat,lng);
                new CountDownTimer((time - System.currentTimeMillis()), 1000){
                    public void onTick(long millisUntilFinished){
                        long cur = (time - System.currentTimeMillis())/1000;
                       textCounter.setText(cur / 3600 + ":" + cur % 3600 / 60 + ":" + cur % 3600 % 60 );
                    }
                    public void onFinish() {
                        time = 0;
                        getRequest();
                    }
                }.start();
                Toast toast = Toast.makeText(MapsActivity.this,"Jerry Position ("+lat+","+lng+")",Toast.LENGTH_LONG);
                toast.show();
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            setUpMap();
        }
    }

    public void postRequest(){
        PostTask pt = new PostTask();
        pt.execute();
    }

    private class PostTask extends AsyncTask<Void, String, String>{
        @Override
        protected String doInBackground(Void... params){
            HttpClient client = new DefaultHttpClient();

            String p_result = null;
            try{
                HttpPost post = new HttpPost ("http://167.205.32.46/pbd/api/catch");
                JSONObject json = new JSONObject();
                json.put("nim", "13512020");
                json.put("token", QRContent);
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

                post.setEntity(se);
                HttpResponse response = client.execute(post);
                if(response != null){
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));

                    String line;
                    StringBuilder sb = new StringBuilder();
                    while((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    p_result = sb.toString();
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return p_result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            String message = "";
            int code = 0;
            String title = "Result";
            try {
                JSONObject json = new JSONObject(result);
                message = json.getString("message");
                code = Integer.parseInt(json.getString("code"));
                if(code == 200){
                    title = "Success!";
                    time = 0;
                    getRequest();
                }
                else if (code == 400){
                    title = "Missing Parameter";
                }
                else if (code == 403){
                    title = "Forbidden";
                }
                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(result);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.setIcon(R.drawable.logo);
                alertDialog.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}