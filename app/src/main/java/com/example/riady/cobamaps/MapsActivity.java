package com.example.riady.cobamaps;

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
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    TextView tvHeading;
    private String jsonResponse;
    private static String TAG = MapsActivity.class.getSimpleName();
    private double lat;
    private double longit;
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private Marker mrk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        makeJsonObjectRequest();

        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);




        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        setUpMapIfNeeded();
    }

    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
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
      //  mMap.addMarker(new MarkerOptions().position(new LatLng(lat, longit)).title("Marker"));
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

    }

    public void JsonRequest(View v){
        makeJsonObjectRequest();

    }

    private void makeJsonObjectRequest() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://167.205.32.46/pbd/api/track?nim=13512024",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                            //Toast.makeText(getApplicationContext(),response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1),Toast.LENGTH_LONG).show();
                            try {
                            JSONObject js= new JSONObject(response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1));
                            lat = js.getDouble("lat");
                            longit = js.getDouble("long");
                            long date = js.getLong("valid_until");

                            Date d = new Date(date*1000L);
                            TextView dtv = (TextView) findViewById(R.id.timeText);

                            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                            dtv.setText("until : "+d.toLocaleString());

                            if(mrk!=null){
                               mrk.remove();

                            }

                            mrk = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, longit))
                                    .title("Jerry")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)));
                            LatLng ll = new LatLng(lat,longit);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll,18));
                            Log.d("tes",String.valueOf(lat));

                            long interval = (date * 1000) - System.currentTimeMillis();

                            Log.d("asd",Long.toString(interval));

                            new CountDownTimer(interval, 1000) {

                                TextView dtv = (TextView) findViewById(R.id.timeText);

                                public void onTick(long millisUntilFinished) {
                                    dtv.setText("Time left : "+Long.toString(millisUntilFinished/3600000)+":"+Long.toString((millisUntilFinished/60000)%60)+":"+Long.toString((millisUntilFinished/1000)%60));
                                }

                                public void onFinish() {
                                    makeJsonObjectRequest();
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

        // Adding request to request queue
            AppController.getInstance().addToRequestQueue(stringRequest, this);
    }

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
            //on catch, show the download dialog
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


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                    StringRequest sr = new StringRequest(Request.Method.POST,"http://167.205.32.46/pbd/api/catch", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject js= new JSONObject(response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1));
                                int code = js.getInt("code");
                                if(code==200){
                                    Toast.makeText(getApplicationContext(),"Jerry berhasil ditangkap",Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {
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
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("nim","13512024");
                            params.put("token",contents);

                            return params;
                        }

                    };
                AppController.getInstance().addToRequestQueue(sr, this);
                }
            }
        }
    }


