package com.ngoprek.tirta.nyobaingooglemaps;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements SensorEventListener{
    final MapsFragment PetaITB = new MapsFragment();
    long validUntil;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    TextView tvHeading;
    JSONObject tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .replace(R.id.layoutMap, PetaITB)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();

        //
        image = (ImageView) findViewById(R.id.pointer);

        // TextView that will tell the user what degree is he heading
        //tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Cek lokasi jerry
        //cekJerry();
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


    //-----------------------KOMPAS---------------------------------
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

    //-----------------------PETA-----------------------------------
    protected void cekJerry(){
        final TextView mTextView = (TextView) findViewById(R.id.textViewNotifikasi);
        mTextView.setText("Starting Connection");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13512004";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
                        JSONObject tracker = null;
                        try {
                            tracker = new JSONObject(response);
                            LatLng loc = new LatLng(tracker.getDouble("lat"), tracker.getDouble("long"));
                            validUntil = tracker.getLong("valid_until");
                            MapsFragment.setJerry(loc,validUntil);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display the response string.
                        //mTextView.setText(tracker.toString());
                        mTextView.setText("Jerry Found!");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("Connection Failed :(");
            }

        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    //-------------------------QR-----------------------------------
    public void scanQR() {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(AndroidBarcodeQrExample.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();

        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                String s = catchJerry(contents);
                //Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
                toast.show();

            }
        }
    }

    public String catchJerry(String token){
        final String mToken = token;

        final TextView mTextView = (TextView) findViewById(R.id.textViewNotifikasi);
        mTextView.setText("Catching Jerry");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/catch";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mTextView.setText("Response: " + response);

                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("Connection Failed :(");
            }

        })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("token",mToken);
                params.put("nim","13512004");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return "JERRY CAUGHT: " + mToken;
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

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_scanner:
                scanQR();
                return true;
            case R.id.action_check:
                cekJerry();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
