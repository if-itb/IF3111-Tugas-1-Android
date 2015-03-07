package com.example.blank.trackjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends FragmentActivity implements SensorEventListener,OnMapReadyCallback {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private float angle;
    private ImageView image;
    private SensorManager s_manager;
    private Sensor g_sens;
    private Sensor m_sens;
    private Context ctxt = this;
    private float[] g = new float[3];
    private float[] m = new float[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        image = (ImageView) findViewById(R.id.kompas);
        s_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        g_sens = s_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sens = s_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        s_manager.registerListener(this, g_sens, SensorManager.SENSOR_DELAY_GAME);
        s_manager.registerListener(this, m_sens, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        s_manager.unregisterListener(this);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_place))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-6.893271, 107.610266)));
        getLoc();
    }
    @Override
    public void onMapReady(GoogleMap m) {
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(m.getMyLocation().getLatitude(),m.getMyLocation().getLongitude())));
    }
    public void setLoc(double lati, double longi, long wakt) {
        Toast toast = Toast.makeText(ctxt,"Lati:"+lati+" Longi:"+longi,Toast.LENGTH_LONG);
        toast.show();
        mMap.addMarker(new MarkerOptions().position(new LatLng(lati, longi)).title("Jerry"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lati, longi)));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date1 = new Date();
        Date date2 = new Date(wakt*1000);
        String time = format.format(date1);
        String time2 = format.format(date2);
        final int days = (int)(date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
        long timeDiff = date2.getTime() - date1.getTime();
        new CountDownTimer(timeDiff, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                String seconds = String.valueOf((millisUntilFinished/1000)%60);
                String minutes = String.valueOf((millisUntilFinished/1000)/60%60);
                String hours = String.valueOf((millisUntilFinished/1000)/60/60%24);
                String dayString = String.valueOf(millisUntilFinished / (1000 * 60 * 60 * 24));
                if(seconds.length() == 1) seconds = "0" + seconds;
                if(minutes.length() == 1) minutes = "0" + minutes;
                if(hours.length() == 1) hours = "0" + hours;
                if(days <= 1) dayString = dayString + " day\n";
                else dayString = "\n";
                ((TextView) findViewById(R.id.limit)).setText("Next: " + dayString + hours + ":" + minutes + ":" + seconds);
            }
            @Override
            public void onFinish() {
                getLoc();
            }
        }.start();

        Log.d("test","Lati:"+lati+" Longi:"+longi);
    }

    public void getLoc(View v){
        getLoc();
    }
    public void getLoc(){
        new ParaGet().execute();
    }

    class ParaGet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512048");
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine())!= null) {
                    response += s;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            s = s.substring(s.indexOf('{'),s.lastIndexOf('}')+1);
            JSONObject jsonObject;
            double lati,longi;
            long wakt;
            try {
                jsonObject = new JSONObject(s);
                lati=jsonObject.getDouble("lat");
                longi=jsonObject.getDouble("long");
                wakt=jsonObject.getLong("valid_until");
                setLoc(lati, longi,wakt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                new ParaPost().execute(contents);
            }
        }
    }

    public void updateHasil(String msg, int status){
        if (status==200) {
            Log.d("msg",msg);
            Toast.makeText(ctxt,"Jerry has been caught!",Toast.LENGTH_LONG).show();
        } else if (status==400){
            Toast.makeText(ctxt,"ERROR 400 MISSING PARAMETER",Toast.LENGTH_LONG).show();
        } else if (status==403){
            Toast.makeText(ctxt,"ERROR 403 FORBIDDEN",Toast.LENGTH_LONG).show();
        }
    }

    class ParaPost extends AsyncTask<String, String, String> {
        private int status;
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            String response = "";
            for(String token: params) {
                HttpResponse httpResponse = null;
                HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");
                try {
                    List<BasicNameValuePair> Parameters = new ArrayList();
                    Parameters.add(new BasicNameValuePair("nim","13512048"));
                    Parameters.add(new BasicNameValuePair("token",token));
                    httpPost.setEntity(new UrlEncodedFormEntity(Parameters));
                    httpResponse = httpClient.execute(httpPost);
                    InputStream content = httpResponse.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine())!= null) {
                        response += s;
                    }
                    status=httpResponse.getStatusLine().getStatusCode();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSONObject jsonObject;
            String msg=new String("");
            int code=400;
            try {
                s=s.substring(s.indexOf('{'), s.lastIndexOf('}') + 1);
                jsonObject = new JSONObject(s);
                msg=jsonObject.getString("message");
                code=jsonObject.getInt("code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateHasil(msg,code);
        }
    }

    public void rotateKompas(float cur_ang) {
        RotateAnimation r = new RotateAnimation(-angle, -cur_ang, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration(500);
        r.setFillAfter(true);
        image.startAnimation(r);
        angle = cur_ang;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                g[0] = alpha * g[0] + (1 - alpha) * event.values[0];
                g[1] = alpha * g[1] + (1 - alpha) * event.values[1];
                g[2] = alpha * g[2] + (1 - alpha) * event.values[2];
                //Log.d("test","acc");
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                m[0] = alpha * m[0] + (1 - alpha) * event.values[0];
                m[1] = alpha * m[1] + (1 - alpha) * event.values[1];
                m[2] = alpha * m[2] + (1 - alpha) * event.values[2];
                //Log.d("test","magn");
            }
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, g, m);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float x;
                x = (float) Math.toDegrees(orientation[0]);
                x = (x + 360) % 360;
                //Log.d("test","rotate");
                rotateKompas(x);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
