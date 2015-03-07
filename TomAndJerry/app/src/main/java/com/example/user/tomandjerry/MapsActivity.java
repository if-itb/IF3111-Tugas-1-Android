package com.example.user.tomandjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.util.LangUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity  implements SensorEventListener, CharSequence {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView image; // define the display assembly compass picture
    private float currentDegree = 0f;// record the compas picture angle turned
    private SensorManager mSensorManager;//device sensor manager
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private Marker JerryPost;
    public long interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        image=(ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);

        getJerryPosition();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        float degree = Math.round(event.values[0]);


        RotateAnimation ra = new RotateAnimation(currentDegree,-degree,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    //product qr code mode
    public void scanQr(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String url="http://167.205.32.46/pbd/api/catch";

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");


                StringRequest sr = new StringRequest(Request.Method.POST,url,new Response.Listener<String>(){
                    @Override
                    public  void onResponse(String response){
                        try{
                            JSONObject js = new JSONObject(response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1));
                            int code = js.getInt("code");

                            if(code == 200){
                                Toast.makeText(getApplicationContext(),"Jerry ketangkap",Toast.LENGTH_LONG).show();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String,String>();
                        params.put("nim","13512090");
                        params.put("token",contents);

                        return params;

                    }
                };
                AppController.getInstance().addToRequestQueue(sr,this);
            }
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
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public void getJerryPosition() {
        String url = "http://167.205.32.46/pbd/api/track?nim=13512090";
        StringRequest stringReq = new StringRequest(Request.Method.GET,url,new Response.Listener<String>(){
            @Override
            public void onResponse (String response){
                try{

                    JSONObject js = new JSONObject(response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1));

                    double longitude = js.getDouble("long");
                    double latitude = js.getDouble("lat");
                    long valid = js.getLong("valid_until");

                    Date date = new Date(valid*1000L);

                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                    if(JerryPost != null) {
                        JerryPost.remove();
                    }

                    JerryPost = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Jerry"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),18));
                    mMap.setMyLocationEnabled(true);


                    interval = (valid * 1000) - System.currentTimeMillis();

                    new CountDownTimer(interval,1000){
                        TextView tf = (TextView) findViewById(R.id.timeLeft);

                        public void onTick(long milisecond){
                            tf.setText("Timeleft: " +Long.toString(milisecond/3600000)+":"+Long.toString((milisecond/60000)%60)+":"+Long.toString((milisecond/1000)%60));

                        }

                        public void onFinish(){
                            getJerryPosition();
                        }
                    }.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(stringReq,this);
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }
}

