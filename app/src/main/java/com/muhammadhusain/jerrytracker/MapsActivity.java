package com.muhammadhusain.jerrytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException; import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements SensorEventListener {
    private GoogleMap mMap;
    private static String url = "http://167.205.32.46/pbd/api/track?nim=13512067";
    private ImageView image;
    private float currentDegree = 0f;
    private static final String TAG_Longitude = "long";
    private static final String TAG_Latitute = "lat";
    private static final String TAG_Valid = "valid_until";
    double Latitude;
    double Longitude;
    Long Valid_until;
    private SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        setUpMapIfNeeded();
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

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                StringRequest catschs = new StringRequest(Request.Method.POST,"http://167.205.32.46/pbd/api/catch", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject js= new JSONObject(response.substring(response.toString().indexOf("{"),response.toString().lastIndexOf("}")+1));
                            int code = js.getInt("code");

                            if(code==200){
                                Toast.makeText(getApplicationContext(),"Jerry berhasil ditangkap" + js.toString(),Toast.LENGTH_LONG).show();
                            }
                            
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "jojojojojo, salah", Toast.LENGTH_LONG).show();
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("nim","13512067");
                        params.put("token",contents);

                        return params;
                    }

                };
                RequestQueue antri = Volley.newRequestQueue(this);
                antri.add(catschs);
            }
        }
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStrw = sh.makeServiceCall(url, ServiceHandler.GET);
            String jsonStr = jsonStrw.substring(jsonStrw.toString().indexOf("{"));
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                        Latitude = jsonObj.getDouble(TAG_Latitute);
                        Longitude = jsonObj.getDouble(TAG_Longitude);
                        Valid_until = jsonObj.getLong(TAG_Valid);
                    }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 15));

            Long remainingTime = Valid_until*1000-System.currentTimeMillis();

            final TextView countdown = (TextView) findViewById(R.id.mtextView);
            new CountDownTimer(remainingTime,1000) {
                public void onTick(long millisUntilFinished) {
                    Long hour, minnute, second;
                    hour = millisUntilFinished/3600000;
                    minnute = (millisUntilFinished-(hour*3600000))/60000;
                    second = (millisUntilFinished/1000)%60;
                    countdown.setText("Update time till: " + hour +":"+ minnute +":"+ second);
                }

                public void onFinish() {
                    countdown.setText("done!");
                    new GetContacts().execute();
                }
            }.start();
        }
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

    public  void spike(View v){
        new GetContacts().execute();
    }
    private void setUpMap() {
        new GetContacts().execute();
    }
}
