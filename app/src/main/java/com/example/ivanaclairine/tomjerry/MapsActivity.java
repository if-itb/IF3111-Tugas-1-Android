package com.example.ivanaclairine.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
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

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements SensorEventListener,LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //TextView view1;
    float lat, lang;
    Marker marker = null;
    public long deadline;
    int status;
    long currtime = System.currentTimeMillis();
    private ImageView image;
    private float currentDegree = 0f;
    TextView text1;
    private SensorManager mSensorManager;
    String contents;
    private boolean lock;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        text1 = (TextView) findViewById(R.id.textTimer);
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        try {
            JSONObject objek = new TrackingTask().execute().get();
            if (objek != null) {
                lat = (float) objek.getDouble("lat");
                lang = (float) objek.getDouble("long");
                deadline = ((long) objek.getLong("valid_until")*1000) - currtime;
                //deadline = 10000;
                createCountDownTimer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setUpMapIfNeeded();
    }

    private void createCountDownTimer() {
        new CountDownTimer(deadline, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
           /*     System.out.println("masuk OnTick");*/
                if(millisUntilFinished/1000 > (3600*24))
                {
                    int hari = (int) millisUntilFinished/1000 / (3600*24);
                    long sisahari =  millisUntilFinished/1000 % (3600*24);
                    int jam = (int) sisahari / (3600);
                    int sisajam = (int) sisahari % 3600;
                    int menit = (int) sisajam / 60;
                    int detik = (int) sisajam%60;
                    text1.setText("Catch Jerry in " + hari + " days " + jam + " hour " + menit + " mins " + detik + " secs ");
                }
                else if (millisUntilFinished/1000 > 3600)
                {
                    int jam = (int) millisUntilFinished/1000 / 3600;
                    long sisajam =  millisUntilFinished/1000 % 3600;
                    int menit = (int) sisajam / 60;
                    int detik = (int) sisajam % 60;
                    text1.setText("Time : " + jam + " hour " + menit + " mins " + detik + " secs ");
                }
                else if (millisUntilFinished/1000 > 60)
                {

                    int menit = (int) millisUntilFinished/1000 / 60;
                    int detik = (int)  millisUntilFinished/1000 % 60;
                    text1.setText("Catch Jerry in " + "\n" + menit + "minutes" + detik + " secs ");
                }
                else
                {
                    text1.setText("Catch Jerry in " + millisUntilFinished/1000 + " secs ");
                }

                deadline = millisUntilFinished;
                setUpMapIfNeeded();
            }

            @Override
            public void onFinish() {
              try {
                    JSONObject objek = new TrackingTask().execute().get();
                    if (objek != null) {
                        lat = (float) objek.getDouble("lat");
                        lang = (float) objek.getDouble("long");
                        deadline = (long) objek.getLong("valid_until")-currtime;
                        /*lat = (float) (lat+0.5);
                        lang= (float) (lang+0.5);
                        deadline = 10000;*/
                        setUpMap();
                        createCountDownTimer();
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


    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
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


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
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
        Marker marker = null;
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lang)).
                icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)).title("Jerry"));
        marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lang), 16));
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {

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

    private class TrackingTask extends AsyncTask<Void, Void, JSONObject> implements com.example.ivanaclairine.tomjerry.TrackingTask {

        @Override
        protected JSONObject doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512041");
            HttpResponse response = null;
            try {
                response = client.execute(request);

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                String message = "";
                while ((line = rd.readLine()) != null) {
                    message += line;
                }

                Log.d("tracking", message);
                return new JSONObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class HttpAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch");
            HttpResponse response = null;

            String json;
            String nim = "13512041";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("token", contents);
                jsonObject.put("nim", nim);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            try {
                request.setEntity(new StringEntity(jsonObject.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //7. set Header
            request.setHeader("Content-type", "application/json");

            try {
                response = client.execute(request);
                // Get the response
                BufferedReader rd = new BufferedReader
                        (new InputStreamReader(response.getEntity().getContent()));

                String line = "";
                String message1 = "";
                while ((line = rd.readLine()) != null) {
                    message1 += line;
                }
                Log.d("tracking", jsonObject.toString());
                return message1;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        // onPostExecute displays the results of the AsyncTask.

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                try {
                    new HttpAsyncTask().execute();
                } catch (Exception e) {

                }
            }
        }
    }

}
