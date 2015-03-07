package rivasyafri.tomjerry;

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
import android.location.GpsStatus;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final String NIM = "13512036";
    private final String Spike = "http://167.205.32.46/pbd/api/catch";

    /**
     * JSON Parsing
     * Get examples from http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
     */

    // URL to get contacts JSON
    private static String url = "http://167.205.32.46/pbd/api/track?nim=13512036";

    // JSON Node names
    private static String TAG_LATITUDE = "lat";
    private static String TAG_LONGITUDE = "long";
    private static String TAG_VALID_UNTIL = "valid_until";

    // Saved variable
    private double latitude;
    private double longitude;
    private long valid_until;

    /**
     * Compass
     * Get examples from http://www.javacodegeeks.com/2013/09/android-compass-code-example.html
      */

    // define the display assembly compass picture
    private ImageView compass_image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    /**
     * Scan QR Code
     * Get examples from http://examples.javacodegeeks.com/android/android-barcode-and-qr-scanner-example/
     */
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    /**
     * Timer
      */
    CountDownTimer timer;
    TextView counterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMap();

        // Set up compass
        compass_image = (ImageView) findViewById(R.id.compass);
        // TextView that will tell the user what degree is he heading
        counterText = (TextView) findViewById(R.id.counterText);
        // Initialize android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

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
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // Calling async task to get json
        new GetLocation().execute();
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
        compass_image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not used
    }

    // Product QR code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
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
                String contents = intent.getStringExtra("SCAN_RESULT");
                Toast toast = Toast.makeText(this, "Sending to Spike:" + contents, Toast.LENGTH_LONG);
                toast.show();
                catchRequest(contents);
            }
        }
    }

    public void catchRequest(String token) {
        JSONObject data = new JSONObject();
        try {
            data.put("nim", NIM);
            data.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Spike, data,
                new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(MapsActivity.this, "You successfully catch Jerry", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MapsActivity.this, "Failed to catch Jerry:" + Integer.toString(error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
                }
            });
        RequestQueue requestQueue = Volley.newRequestQueue(MapsActivity.this);
        requestQueue.add(request);
    }

    /**
     * Async task class to get json by making HTTP call
     * Get from http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
     * */
    private class GetLocation extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            //String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            //jsonStr = jsonStr.substring(jsonStr.toString().indexOf('{'), jsonStr.toString().indexOf('}') + 1);
            String jsonStrRaw = sh.makeServiceCall(url, ServiceHandler.GET);
            String jsonStr;
            jsonStr = jsonStrRaw.substring(jsonStrRaw.toString().indexOf('{'), jsonStrRaw.toString().lastIndexOf('}') + 1);
            Log.d("Response: ", "> " + jsonStr);

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
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // Set Jerry's Position
            LatLng jerryLoc = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerryLoc, 18));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry's Location").visible(true));
            final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER ) ) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
            }

            // Set Text
            Long timeRemaining = valid_until*1000 - System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), String.valueOf(timeRemaining), Toast.LENGTH_LONG).show();
            timer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Long hours, minutes, seconds;
                    hours = millisUntilFinished/3600000;
                    minutes = (millisUntilFinished/60000)%60;
                    seconds = (millisUntilFinished/1000)%60;
                    counterText.setText("Jerry's next move in : " + hours + ":" + minutes + ":" + seconds);
                }

                @Override
                public void onFinish() {
                    new GetLocation().execute();
                }
            }.start();
        }

    }
}
