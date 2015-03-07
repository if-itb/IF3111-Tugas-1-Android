package com.pbd.fauzanhilmi.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static String url = "http://167.205.32.46/pbd/api/track";
    private static String urlpost = "http://167.205.32.46/pbd/api/catch";
    protected String[] comps;
    private double lat;
    private double lon;
    private long valid_until;
    private String token;
    protected JSONObject jsonpost;
    protected String POSTresponse;
    private ImageView compassimage;
    private SensorManager mSensorManager;
    private float curDeg = 0f;
    private TextView textLatLong;
    private TextView textTimer;
    public long timenow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        token = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        new GetContacts().execute();
        while(comps==null) {}
        comps[10] = comps[10].substring(1, comps[10].length()-1);
        lat = Double.parseDouble(comps[3]);
        lon = Double.parseDouble(comps[7]);
        valid_until = Long.parseLong(comps[10]);
        textLatLong = (TextView) findViewById(R.id.textLatLong);
        textLatLong.setText("Lat: "+lat+"\nLong: "+lon);
        textTimer = (TextView) findViewById(R.id.textTimer);

        timenow = System.currentTimeMillis();
        valid_until = valid_until * 1000;

        CountDownTimer start = new CountDownTimer(valid_until-System.currentTimeMillis(), 1000) {
            public void onTick(long milisuntilend) {
                long cur = (valid_until - System.currentTimeMillis()) / 1000;
                textTimer.setText("Valid time remaining:\n" + cur / 3600 + ":" + cur % 3600 / 60 + ":" + cur % 3600 % 60);
                //textTimer.setText("This location valid until :\n"+cur/(3600*24)+" day "+(cur / 3600) % 24+" hour "+cur/(3600*24)+" min "+cur%(3600*24)%60%60+" sec");
            }

            @Override
            public void onFinish() {
                Refresh();
            }
        }.start();
        setUpMapIfNeeded();
    }

    public void Refresh() {
        new GetContacts().execute();
        while(comps==null) {}
        comps[10] = comps[10].substring(2, comps[10].length()-1);
        lat = Double.parseDouble(comps[3]);
        lon = Double.parseDouble(comps[7]);
        valid_until = Long.valueOf(comps[10]).longValue();
        textLatLong = (TextView) findViewById(R.id.textLatLong);
        textLatLong.setText("Lat: "+lat+"\nLong: "+lon);
        textTimer = (TextView) findViewById(R.id.textTimer);

        Calendar c = Calendar.getInstance();
        final long timenow = c.get(Calendar.SECOND);
        final long dif = valid_until-timenow;

        Toast toast = Toast.makeText(this,"Information refreshed...",Toast.LENGTH_LONG);
        toast.show();

        valid_until = System.currentTimeMillis()+10000;
        CountDownTimer start = new CountDownTimer(valid_until-System.currentTimeMillis(), 1000) {
            public void onTick(long milisuntilend) {
                long cur = (valid_until - System.currentTimeMillis()) / 1000;
                textTimer.setText("Valid time remaining:\n" + cur / 3600 + ":" + cur % 3600 / 60 + ":" + cur % 3600 % 60);
                //textTimer.setText("This location valid until :\n"+cur/(3600*24)+" day "+(cur / 3600) % 24+" hour "+cur/(3600*24)+" min "+cur%(3600*24)%60%60+" sec");
            }

            @Override
            public void onFinish() {
                Refresh();
            }
        }.start();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
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
                	                curDeg,
                	                -degree,
                	                Animation.RELATIVE_TO_SELF, 0.5f,
                	                Animation.RELATIVE_TO_SELF,
                	                0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        compassimage.startAnimation(ra);
        curDeg = -degree;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing
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
                compassimage = (ImageView) findViewById(R.id.compass);
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Jerry"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    public void qrButtonClicked(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void locationButtonClicked(View view) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        POSTresponse = null;
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                token = contents;

                try {
                    String message = "{\"nim\": \"13512003\", \"token\": \""+token+"\"}";
                    jsonpost = new JSONObject(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new GetContactsPost().execute();
                Toast toast = Toast.makeText(this,"Token received : "+token+"\nNow sending token to server. Please wait...",Toast.LENGTH_LONG);
                toast.show();
                while(POSTresponse==null) {}
                toast = Toast.makeText(this,"Token sent. Server response :"+POSTresponse,Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    //classes for GET/POST request
    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            if (jsonStr != null) {
                comps = jsonStr.split("\"");
            }
            else
            {
                comps = null;
            }
            return null;
        }
    }

    private class GetContactsPost extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(urlpost, ServiceHandler.POST, jsonpost);
            if (jsonStr != null) {
                POSTresponse = jsonStr;
            }
            else
            {
                POSTresponse = "Couldn't get any data from the url";
            }
            return null;
        }
    }
}


