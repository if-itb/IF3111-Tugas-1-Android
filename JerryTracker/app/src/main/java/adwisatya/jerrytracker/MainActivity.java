package adwisatya.jerrytracker;

/**
 * Created by : Aryya Dwisatya W - 13512034
 * adwisatya
 * Compass reference: http://www.javacodegeeks.com/2013/09/android-compass-code-example.html
 */
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements SensorEventListener {

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    TextView txtToken;
    TextView txtLat;
    TextView txtLong;
    TextView txtValid;
    Button scanner;
    GPSTracker gps;
    int it = 0;
    double jLat = 0, jLong=0;

    //static LatLng JerryLocation = new LatLng(0,0);
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private GoogleMap googleMap;

    JSONArray LatLong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* gambarkan layout activity */
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageViewCompass);
        txtToken = (TextView) findViewById(R.id.txtToken);
        txtLong = (TextView) findViewById(R.id.txtLong);
        txtLat = (TextView) findViewById(R.id.txtLat);
        txtValid =  (TextView) findViewById(R.id.txtValid);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gps = new GPSTracker(MainActivity.this);

        //updateJerryLocation(JerryLocation);
        setUpMap();
        scanner = (Button) findViewById(R.id.scanner);

    }

    public void scanQR(View v){
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
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
                txtToken.setText(contents);
            }
        }
    }
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
    public void onSensorChanged(SensorEvent event){
        float degree = Math.round(event.values[0]);
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(90);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;

        it++;
        if(it>20) {
            new HttpAsyncTask().execute();
            updateJerryLocation();
            it = 0;
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setUpMap(){
        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMyLocationEnabled(true);
            Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(new LatLng(gps.getLatitude(),gps.getLongitude())));
            //TP.setPosition(new LatLng(0,0));
            //TP.setPosition(new LatLng(Double.parseDouble(txtLat.toString()),Double.parseDouble(txtLong.toString())));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(),gps.getLongitude()),15));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateJerryLocation(){
        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.clear();
            googleMap.setMyLocationEnabled(true);
            Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(new LatLng(jLat,jLong)));
            //TP.setPosition(new LatLng(Double.parseDouble(txtLat.toString()),Double.parseDouble(txtLong.toString())));
            //TP.setPosition(new LatLng(-6.890323,107.610381));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(txtLat.toString()),Double.parseDouble(txtLong.toString())),15));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ambilLokasiJerry(){
        LatLng LJerry;
        String url = "http://167.205.32.46/pbd/api/track?nim=13512043";
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
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
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

            return ambilLokasiJerry();
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            try{
                JSONObject jsonObj = new JSONObject(result);
                txtLat.setText(jsonObj.getString("lat"));
                jLat = jsonObj.getDouble("lat");
                txtLong.setText(jsonObj.getString("long"));
                jLong = jsonObj.getDouble("long");
                txtValid.setText(simpleDateFormat.format(jsonObj.getDouble("valid_until")));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void postToker(){
        String token = txtToken.toString();
        JSONObject final_data = new JSONObject();
        try {
            final_data.put("nim", "13512043");
            final_data.put("token", token);
        }catch(JSONException e){
            e.printStackTrace();
        }

        HttpClient httpClient =  new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

        try{
            httpPost.setEntity(new StringEntity(final_data.toString()));
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

        try{
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpClient.execute(httpPost);
            Log.d("Http Response:", response.toString());
            Toast.makeText(getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();

        }catch(ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}