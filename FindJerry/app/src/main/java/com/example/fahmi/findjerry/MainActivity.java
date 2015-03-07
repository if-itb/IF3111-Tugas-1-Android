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
import android.os.CountDownTimer;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

/**
 * Created by Fahmi on 07/03/2015.
 */
public class MainActivity extends Activity implements SensorEventListener {
    //buat Map
    private GoogleMap Peta;
    //buat Compass
    // define the display assembly compass picture
    private ImageView image;
    private ImageView imageTom;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    //buat QRCodeScanner
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private long time = 0;
    LatLng PosisiJerry;
    TextView txtLatitude;
    TextView txtLongitude;
    TextView txtValidUntil;
    TextView txtToken;
    TextView txtResponse;
    String token;
    GPSTracker GPS;
    Button scanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);
        imageTom = (ImageView) findViewById(R.id.imageViewTom);
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        scanner = (Button) findViewById(R.id.scanner);
        txtToken = (TextView) findViewById(R.id.txtToken);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtValidUntil = (TextView) findViewById(R.id.txtValidUntil);
        txtResponse = (TextView) findViewById(R.id.txtResponse);
        GPS = new GPSTracker(MainActivity.this);

        setUpMap();
        GetLocationJerry();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        Peta.setMyLocationEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
        Peta.setMyLocationEnabled(false);
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
        // not in use
    }


    public void scanQR(View v) {
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
                txtToken.setText("Token : " + contents);
                token = contents;
                TangkapJerry();
            }
        }
    }

    public void setUpMap() {
        try {
            if (Peta == null) {
                Peta = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            }
            Peta.setMyLocationEnabled(true);
            Marker TP = Peta.addMarker(new MarkerOptions().position(PosisiJerry).icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry_icon)).title("Persembunyian Jerry"));
            Peta.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(PosisiJerry.latitude,PosisiJerry.longitude), 15));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetLocationJerry() {
        GetTask GT = new GetTask();
        GT.execute("http://167.205.32.46/pbd/api/track?nim=13512047");
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
            try {
                HttpResponse response = (HttpResponse) client.execute(request);
                entity = response.getEntity();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                try {
                    json = new JSONObject(sb.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
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
            try {
                lat = Double.parseDouble(result.getString("lat"));
                lng = Double.parseDouble(result.getString("long"));
                txtLatitude.setText("Latitude: "+lat);
                txtLongitude.setText("Longitude: "+lng);
                PosisiJerry = new LatLng(lat,lng);
                time = Long.parseLong(result.getString("valid_until")) * 1000;
                new CountDownTimer((time - System.currentTimeMillis()), 1000) {
                    public void onTick(long millisUntilFinished) {
                        long cur = (time - System.currentTimeMillis()) / 1000;
                        txtValidUntil.setText("Waktu Jerry Sembunyi\n" + cur / 3600 + ":" + cur % 3600 / 60 + ":" + cur % 3600 % 60);
                    }

                    public void onFinish() {
                        time = 0;
                        GetLocationJerry();
                    }
                }.start();
                Toast toast = Toast.makeText(MainActivity.this, "Posisi Jerry sekarang (" + lat + "," + lng + ")", Toast.LENGTH_LONG);
                toast.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpMap();
        }
    }

    public void TangkapJerry() {
        PostTask pt = new PostTask();
        pt.execute();
    }

    private class PostTask extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            String result = null;
            try {
                HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                JSONObject json = new JSONObject();
                json.put("nim", "13512047");
                json.put("token", token);
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                HttpResponse response = client.execute(post);
                if (response != null) {
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    result = sb.toString();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String message = "";
            int code = 0;
            String title = "Result";
            try {
                JSONObject json = new JSONObject(result);
                message = json.getString("message");
                code = Integer.parseInt(json.getString("code"));
                if(code == 200){
                    title = "YEAH! Jerry berhasil tertangkap";
                }
                Toast toast = Toast.makeText(MainActivity.this, title , Toast.LENGTH_LONG);
                toast.show();
                txtResponse.setText(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

