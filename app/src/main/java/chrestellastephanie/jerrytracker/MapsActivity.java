package chrestellastephanie.jerrytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //url to get JSON
    private static String url = "http://167.205.32.46/pbd/api/track?nim=13512005";
    private static String urlPost = "http://167.205.32.46/pbd/api/catch";
    //JSON node names
    private static String TAG_LATITUDE = "lat";
    private static String TAG_LONGITUDE = "long";
    private static String TAG_VALID_UNTIL = "valid_until";
    private String token;
    double latitude;
    double longitude;
    Long valid_until;
    Button qrCode;
    Button askSpike;
    private TextView countDown;
    private SensorManager mSensorManager;
    private ImageView image;
    private float currentDegree = 0f;

    //    QR code
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //setUpMapIfNeeded();
        setUpMap();
        countDown = (TextView) findViewById(R.id.countDown);
        qrCode = (Button) findViewById(R.id.qrCode);
        qrCode.setText("Catch Jerry");
        askSpike = (Button) findViewById(R.id.askSpike);
        askSpike.setText("Ask Spike");
        image = (ImageView) findViewById(R.id.compass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        //countDown.setText("Jerry will move in :  awal" /*+ TimeCounter.remainingTime*/);
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

    private class GetPosition extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            this.pDialog = ProgressDialog.show(MapsActivity.this, "", "Please wait.....");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            //make request to url
            String jsonStrRaw = sh.makeServiceCall(url, ServiceHandler.GET);
            String jsonStr;
            jsonStr = jsonStrRaw.substring(jsonStrRaw.toString().indexOf("{"), jsonStrRaw.toString().lastIndexOf("}") + 1);

            Log.d("Response : ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    latitude = jsonObj.getDouble(TAG_LATITUDE);
                    longitude = jsonObj.getDouble(TAG_LONGITUDE);
                    valid_until = jsonObj.getLong(TAG_VALID_UNTIL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(pDialog.isShowing()) {
                super.onPostExecute(result);
                //dismiss progress dialog
                this.pDialog.dismiss();
            }
            //Toast.makeText(MapsActivity.this, latitude+" "+longitude, Toast.LENGTH_LONG).show();
            LatLng jerryPos = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerryPos, 18));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry is Here!").visible(true));
            mMap.setMyLocationEnabled(true);

            //timer
            Long remainingTime = valid_until*1000-System.currentTimeMillis();
            //Toast.makeText(getApplicationContext(), String.valueOf(remainingTime), Toast.LENGTH_LONG).show();
            CountDownTimer timer = new CountDownTimer(remainingTime,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Long hour, minute, second;
                    hour=millisUntilFinished/3600000;
                    minute=(millisUntilFinished-(hour*3600000))/60000;
                    second=(millisUntilFinished/1000)%60;
                    countDown.setText("Jerry will move in: " + hour + " : " + minute + " : " + second);
                }
                @Override
                public void onFinish() {
                    countDown.setText("Jerry is not there anymore :(");
                    new GetPosition().execute();
                }
            }.start();
        }
    }


    private class postCatch extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = null;
        private String response;
        private String code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress dialog
            this.pDialog = ProgressDialog.show(MapsActivity.this, "", "Please wait.....");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nim","13512005"));
            params.add(new BasicNameValuePair("token",token));
            String jsonStr = sh.makeServiceCall(urlPost,ServiceHandler.POST, params);
            try{
                response = new String(jsonStr.getBytes("ISO-8859-1"),"utf-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing()) {
                //dismiss progress dialog
                this.pDialog.dismiss();
            }
//            Toast.makeText(MapsActivity.this, response,Toast.LENGTH_LONG).show();
            try{
                JSONObject jsonObj = new JSONObject(response);
                code=jsonObj.getString("code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Toast.makeText(MapsActivity.this, code ,Toast.LENGTH_LONG).show();
            if (code.equals("200")){
                Toast.makeText(MapsActivity.this, "Jerry is captured. Congratulations!" ,Toast.LENGTH_LONG).show();
            } else if(code.equals("403")){
                Toast.makeText(MapsActivity.this, "Jerry is not here :(",Toast.LENGTH_LONG).show();
            } else if( code.equals("400")){
                Toast.makeText(MapsActivity.this, "wrong parameter!",Toast.LENGTH_LONG).show();
            }

        }
    }






    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        new GetPosition().execute();
    }

    public void askSpike(View v){
        new GetPosition().execute();
    }

    /*QR code*/
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
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
        ServiceHandler sh = new ServiceHandler();
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                token=contents;
                //  Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
//                toast.show();
                new postCatch().execute();



            }
        }
    }

}

