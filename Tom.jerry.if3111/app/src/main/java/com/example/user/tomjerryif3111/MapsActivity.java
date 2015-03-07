package com.example.user.tomjerryif3111;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements SensorEventListener{
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private JSONObject token;
    private JSONObject dataJSON;
    private double lat;
    private double lon;
    private double valid_until;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView image;// define the display assembly compass picture
    private float currentDegree = 0f; // record the compass picture angle turned
    private SensorManager mSensorManager; // device sensor manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        setUpMap();
        token = null;
        dataJSON = null;
        image = (ImageView) findViewById(R.id.imageViewCompass); // our compass image
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // initialize your android device sensor capabilities
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
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
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if(mMap != null) {
            new Location().execute("http://167.205.32.46/pbd/api/track?nim=13512023");
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Lokasi"));
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 17.0f));
            new Timer().execute();
        }
        else{
            setUpMapIfNeeded();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);// get the angle around the z-axis rotated
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);// create a rotation animation (reverse turn degree degrees)
        ra.setDuration(210);// how long the animation will take place
        ra.setFillAfter(true);// set the animation after the end of the reservation status
        image.startAnimation(ra);// Start the animation
        currentDegree = -degree;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void scanQRCode(View view) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                writeJSON(contents);
                sendToken(token);
                setUpMapIfNeeded();
                setUpMap();
            }
        }
    }
    public void writeJSON(String tokenString) {
        try {
            token = new JSONObject("{\"nim\":\"13512023\",\"token\":\"" + tokenString + "\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void sendToken(JSONObject token) { // mengirim token QR Code ke server
        HttpClient mHttpClient = null;
        HttpPost mHttpPost = null;
        int status;
        try {
            mHttpClient = new DefaultHttpClient();
            mHttpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");
            StringEntity mStringEntity = new StringEntity(token.toString());
            mStringEntity.setContentEncoding("UTF-8");
            mStringEntity.setContentType("application/json");

            mHttpPost.setEntity(mStringEntity);
            HttpResponse mResponse = mHttpClient.execute(mHttpPost);
            status = mResponse.getStatusLine().getStatusCode();

            Toast toast = null;
            //Log.d(TAG, "status: " + status);
            if (mResponse != null && (status == 400 || status == 403)) {
                toast = Toast.makeText(this, "Gagal mengirimkan token", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(status == 200){
                try {
                    toast = Toast.makeText(this, token.getString("nim") + "; " + token.getString("token"), Toast.LENGTH_LONG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                toast.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
    private class Location extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(String.valueOf(params[0]));
            try {
                // Get the response
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                //while (statusCode != 200) {}
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    content.close();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result;
            if (builder.length() != 0) {
                result = builder.toString();
                System.out.println(result + "\n");
                try {
                    dataJSON = new JSONObject(result);
                    lat = dataJSON.getDouble("lat");
                    lon = dataJSON.getDouble("long");
                    valid_until = dataJSON.getDouble("valid_until");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        protected void onPostExecute(Void v){
            setUpMapIfNeeded();
            setUpMap();
        }
    }
    private class Timer extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            boolean stop = false;
            long now = System.currentTimeMillis();
            while(!stop){
                if(valid_until <= now){
                    stop = true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
                now = System.currentTimeMillis();
            }
            return null;
        }
        protected void onPostExecute(Void v){
            new Location().execute("http://167.205.32.46/pbd/api/track?nim=13512023");
        }
    }
}
