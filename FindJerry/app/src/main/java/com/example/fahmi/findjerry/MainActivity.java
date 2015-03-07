package com.example.fahmi.findjerry;

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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fahmi on 07/03/2015.
 */
public class MainActivity extends Activity implements SensorEventListener{
    //buat Map
    private GoogleMap Peta;
    //buat Compass
    // define the display assembly compass picture
    private ImageView image;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    //buat QRCodeScanner
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    TextView tvHeading;
    TextView txtLatitude;
    TextView txtLongitude;
    TextView txtValidUntil;
    TextView txtToken;
    TextView txtResponse;
    String token;
    GPSTracker GPS;
    Button btnSubmit;
    Button scanner;
    double JerryLat = 0;
    double JerryLong = 0;
    int it = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);
        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        scanner = (Button) findViewById(R.id.scanner);
        txtToken = (TextView) findViewById(R.id.txtToken);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtValidUntil = (TextView) findViewById(R.id.txtValidUntil);
        txtResponse = (TextView) findViewById(R.id.txtResponse);
        GPS = new GPSTracker(MainActivity.this);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                try {
                    txtResponse.setText(new MyAsyncTask().execute().get());
                }catch(InterruptedException e){
                    e.printStackTrace();
                }catch(ExecutionException e){
                    e.printStackTrace();
                }
            }
        });
        setUpMap();
    }

    @Override
    protected void onResume() {
        super.onResume();

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


        it++;
        if(it>20) {
            new HttpAsyncTask().execute();
            UpdateLocationJerry();
            it = 0;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }


    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
                txtToken.setText(contents);
                token = contents;
            }
        }
    }
    public void setUpMap(){
        try {
            if (Peta == null) {
                Peta = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            }
            Peta.setMyLocationEnabled(true);
            Marker TP = Peta.addMarker(new MarkerOptions().position(new LatLng(GPS.getLatitude(), GPS.getLongitude())));
            Peta.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(GPS.getLatitude(), GPS.getLongitude()), 15));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String GetLocationJerry(){
        String url = "http://167.205.32.46/pbd/api/track?nim=13512047";
        InputStream inputStream = null;
        String hasil = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                hasil = convertInputStreamToString(inputStream);
            else
                hasil = "Gagal cuy!";
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return hasil;
    }
    public void UpdateLocationJerry(){
        try {
            if (Peta == null) {
                Peta = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            Peta.clear();
            Peta.setMyLocationEnabled(true);
            Marker TP = Peta.addMarker(new MarkerOptions().
                    position(new LatLng(JerryLat,JerryLong)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String PostingData(){
        String URL = "http://167.205.32.46/pbd/api/catch";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URL);
        InputStream inputStream = null;
        String result = "";
        JSONObject dataToJSON = new JSONObject();
        StringEntity SE;
        try{
            dataToJSON.put("nim","13512047");
            dataToJSON.put("token",token);
            try {
                SE = new StringEntity(dataToJSON.toString());
                httpPost.setEntity(SE);
                HttpResponse response = httpClient.execute(httpPost);
                inputStream = response.getEntity().getContent();
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else {
                    result = "Gagal gan!";
                }
            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GetLocationJerry();
        }
        @Override
        protected void onPostExecute(String result) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            try{
                JSONObject jsonObj = new JSONObject(result);
                txtLatitude.setText(jsonObj.getString("lat"));
                JerryLat = jsonObj.getDouble("lat");
                txtLongitude.setText(jsonObj.getString("long"));
                JerryLong = jsonObj.getDouble("long");
                txtValidUntil.setText(simpleDateFormat.format(jsonObj.getDouble("valid_until")));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    private class MyAsyncTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params){
            return PostingData();
        }
    }
}
