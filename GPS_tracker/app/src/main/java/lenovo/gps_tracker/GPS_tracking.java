package lenovo.gps_tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class GPS_tracking extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private static String url = "http://167.205.32.46/pbd/api/track?nim=13512075";
    static double latitude, longitude;
    static long valid;
    double lat, lgt;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    static TextView period;
    static TextView expire;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    static String contents;
    static String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_tracking);

        image = (ImageView) findViewById(R.id.imageViewCompass);
        //period.setText("");

        // TextView that will tell the user what degree is he heading
        //tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system, )'s orientation sensor registered listeners

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        //tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

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
        new getInfo().execute();
    }

    private class getInfo extends AsyncTask<Void, Void, Void> {

        public ProgressDialog pDialog;
        ArrayList<HashMap<String, String>> contactList;
        long now;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(GPS_tracking.this);
            pDialog.setMessage("Searching for Jerry...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            serviceHandler sh = new serviceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, serviceHandler.GET);
            System.out.println(jsonStr);
            Log.d("Response", "> " + jsonStr.substring(3));
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr.substring(3));

                    longitude = jsonObj.getDouble("long");
                    valid = jsonObj.getLong("valid_until");
                    latitude = jsonObj.getDouble("lat");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Date valid_until = new Date(valid*1000);
            CountDownTimer count;
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));

            String time = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(valid_until);
            period = (TextView) findViewById(R.id.valid_until);
            period.setText("Valid until : " + time);

            Date date = new Date();
            now =  date.getTime();

//            String now = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date(l));
//            period = (TextView) findViewById(R.id.valid_until);
//            period.setText("Valid until : " + now);

            expire = (TextView) findViewById(R.id.expire);
            new CountDownTimer(valid*1000 - now, 1000) {

                public void onFinish() {
                    expire.setText("");
                    setUpMap();
                }

                public void onTick(long millisUntilFinished) {
                    expire.setText(millisUntilFinished / 1000 + "seconds left");
                }
            }.start();
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
            showDialog(GPS_tracking.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                //get the extras that are returned from the intent
                contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
            }
        }
        new HttpAsyncTask().execute("http://167.205.32.46/pbd/api/catch");
    }

    public static String Post(String url) {

        InputStream input = null;
        String result = "";

        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            JSONObject jsonObject = new JSONObject();
            String json = "";
            jsonObject.accumulate("nim", "13512075");
            jsonObject.accumulate("token", contents);

            json = jsonObject.toString();
            StringEntity stringEntity = new StringEntity(json);
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);
            input = httpResponse.getEntity().getContent();
            if(input != null) {
                result = convertInputStreamToString(input);
            }
            else {
                result = "Didn't work";
            }

            int response = httpResponse.getStatusLine().getStatusCode();
            code = String.valueOf(response);

        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return Post(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String report;
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();

//            if(code.compareTo("200")==0) {
//                report = "Jerry has been caught";
//            }
//            else if(code.compareTo("400")==0) {
//                report = "MISSING PARAMETER";
//            }
//            else {
//                report = "FORBIDDEN";
//            }
            Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
