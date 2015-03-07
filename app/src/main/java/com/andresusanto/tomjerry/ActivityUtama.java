package com.andresusanto.tomjerry;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Debug;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class ActivityUtama extends ActionBarActivity implements SensorEventListener, LocationListener {
    public static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private ImageView imgCompass;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private WebView wvLoading;
    private TextView tvLoading;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private Location lokasi;
    private int secondWait = 0;
    private LatLng posJerry;
    private boolean jerryFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_utama);

        wvLoading = (WebView) findViewById(R.id.loader);
        tvLoading = (TextView) findViewById(R.id.loaderText);

        wvLoading.loadUrl("file:///android_asset/loading.gif");

        imgCompass = (ImageView) findViewById(R.id.compass_img);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        lokasi = locationManager.getLastKnownLocation(provider);
        onLocationChanged(lokasi);


        if (lokasi != null) {
            tvLoading.setText("Contacting spike, please wait ...");
            new UpdateJerry().execute();
        }
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    public void scanBar() {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            wvLoading.setVisibility(View.INVISIBLE);
            tvLoading.setText("QR Scanner not found!");
            tvLoading.setTextColor(Color.RED);
        }
    }

    public boolean laporSpike(String token){
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

            String json = "";

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("nim", "13512028");
            jsonObject.accumulate("secret_token", token);

            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);
            Log.i("SPIKE RESULT", httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
            if(httpResponse.getStatusLine().getStatusCode() == 200)
                return true;


        } catch (Exception e) {
            Log.d("Yo", e.getLocalizedMessage());
        }

        return false;
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                //String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                //Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                //toast.show();

                wvLoading.setVisibility(View.VISIBLE);
                tvLoading.setText("Reporting to spike, please wait ...");
                tvLoading.setTextColor(Color.BLACK);

                new LaporWorker(contents).execute();
            }else{
                wvLoading.setVisibility(View.INVISIBLE);
                tvLoading.setText("QR Scan is cancelled!");
                tvLoading.setTextColor(Color.RED);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_utama, menu);
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
        float degree = Math.round(event.values[0]);
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);

        imgCompass.startAnimation(ra);
        currentDegree = -degree;
    }

    public void clicker(View v){
        jerryFound = true;

        wvLoading.setVisibility(View.INVISIBLE);
        tvLoading.setText("Jerry found! Confirming QR ...");
        tvLoading.setTextColor(Color.GREEN);

        Log.i("Panggil QR Scan", "Panggil QR Scan -> Lat : " + lokasi.getLatitude() + " Lng : " + lokasi.getLongitude());

        scanBar();
    }

    @Override
    public void onLocationChanged(Location location) {
        lokasi = location;
        if (lokasi != null && posJerry != null) {
            Location jerry = new Location("GPS");
            jerry.setLatitude(posJerry.latitude);
            jerry.setLongitude(posJerry.longitude);

            if (lokasi.distanceTo(jerry) < 2f && !jerryFound) {
                jerryFound = true;

                wvLoading.setVisibility(View.INVISIBLE);
                tvLoading.setText("Jerry found! Confirming QR ...");
                tvLoading.setTextColor(Color.GREEN);

                Log.i("Panggil QR Scan", "Panggil QR Scan -> Lat : " + lokasi.getLatitude() + " Lng : " + lokasi.getLongitude());

                scanBar();
            }
        }
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

    private class CounterWaktu extends CountDownTimer {

        public CounterWaktu(long secondInFuture) {
            super(secondInFuture * 1000, 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (jerryFound){
                this.cancel();
                return;
            }

            if (secondWait > 0){
                secondWait -= 1;
                tvLoading.setText(secondWait + " second(s) until jerry moves.");
            }else{
                tvLoading.setText(millisUntilFinished + " tes.");
            }
        }

        @Override
        public void onFinish() {
            wvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("Contacting spike, please wait ...");
            tvLoading.setTextColor(Color.BLACK);
            new UpdateJerry().execute();
        }
    }

    private class LaporWorker extends  AsyncTask<Void, Void, Boolean>{
        private String _token;

        LaporWorker(String token){
            _token = token;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                wvLoading.setVisibility(View.INVISIBLE);
                tvLoading.setText("Congrats! Jerry is caught!");
                tvLoading.setTextColor(Color.GREEN);
            }else{
                wvLoading.setVisibility(View.INVISIBLE);
                tvLoading.setText("QR Code Rejected by Spike");
                tvLoading.setTextColor(Color.RED);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return laporSpike(_token);
        }
    }

    private class UpdateJerry extends AsyncTask<Void, Void, String> {
        private ArrayList<LatLng> arrDirection = new ArrayList<LatLng>();
        private Document doc;


        @Override
        protected void onPostExecute(String result) {
            if (result == null){
                wvLoading.setVisibility(View.INVISIBLE);
                tvLoading.setText("Failed to contact spike");
                tvLoading.setTextColor(Color.RED);
            }else{
                try {
                    JSONObject jObject = new JSONObject(result);
                    long valid = jObject.getLong("valid_until");
                    LatLng posUser = new LatLng(lokasi.getLatitude(), lokasi.getLongitude());

                    MarkerOptions marker = new MarkerOptions().position(posJerry).title("Posisi Jerry");
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                    PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.BLUE);
                    for(int i = 0 ; i < arrDirection.size() ; i++)
                    {
                        rectLine.add(arrDirection.get(i));
                    }


                    secondWait = (int)((valid - System.currentTimeMillis()));
                    secondWait = secondWait / 1000;


                    if (secondWait >= 0) {

                        googleMap.clear();
                        googleMap.addMarker(marker);
                        googleMap.addPolyline(rectLine);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(createLatLngBoundsObject(posJerry, posUser), 50));

                        wvLoading.setVisibility(View.INVISIBLE);
                        tvLoading.setText(secondWait + " second(s) until jerry moves.");
                        tvLoading.setTextColor(Color.GREEN);

                        new CounterWaktu(secondWait).start();

                    }else{
                        wvLoading.setVisibility(View.INVISIBLE);
                        tvLoading.setText("Spike is drunk at the moment. (1)");
                        tvLoading.setTextColor(Color.RED);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    wvLoading.setVisibility(View.INVISIBLE);
                    tvLoading.setText("Spike is drunk at the moment. (2)");
                    tvLoading.setTextColor(Color.RED);
                }

            }
        }

        @Override
        protected String doInBackground(Void... params) {
            InputStream inputStream = null;
            String result = null;

            try {

                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpResponse response = httpclient.execute(new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512028"));
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();

                JSONObject jObject = new JSONObject(result);

                posJerry = new LatLng(jObject.getDouble("lat"), jObject.getDouble("long"));
                LatLng posUser = new LatLng(lokasi.getLatitude(), lokasi.getLongitude());


                DirectionTool md = new DirectionTool();
                doc = md.getDocument(posUser, posJerry, DirectionTool.MODE_WALKING);
                arrDirection = md.getDirection(doc);

            } catch (JSONException e){
                result = "";
            } catch (Exception e) {
                result = null;
            } finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }

            return result;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MAINAN", "MASUK RESUMME");

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        locationManager.requestLocationUpdates(provider, 700, 0.4f, this);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // biar hemat baterai
        locationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
