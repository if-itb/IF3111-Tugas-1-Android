package cilviasianora.jerrycatcher;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    TextView textTime;

    Double lat,lng;
    Long valid_until;
    Long timeNow = System.currentTimeMillis();
    Long intervalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lat = 0.0; lng = 0.0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        new TaskGet().execute(getApplicationContext());

        textTime = (TextView) findViewById(R.id.textTime);
    }

    private void timer(){
        new CountDownTimer(intervalTime, 1000) {

            public void onTick(long millisUntilFinished) {
                intervalTime = millisUntilFinished;
                textTime.setText("seconds remaining: " + (intervalTime/1000));
            }

            public void onFinish() {
                new TaskGet().execute(getApplicationContext());
                intervalTime = (valid_until*1000) - timeNow ;
                setUpMapIfNeeded();
                timer();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
        /*Marker m = null;
        m =*/ mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("Jerry"));
        // Move the camera instantly to location with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    public class TaskGet extends AsyncTask<Context, String, String> {

        @Override
        protected String doInBackground(Context... params) {
            String url = "http://167.205.32.46/pbd/api/track?nim=13512027";

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = null;
            String result = "";

            try {
                response = client.execute(request);

                // get response from server
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";

                while ((line = rd.readLine()) != null) {
                    result+=line;
                }

                Log.d("Result",result);

                // parse the json got from server
                JSONObject json = new JSONObject(result);
                lat = json.getDouble("lat");
                lng = json.getDouble("long");
                valid_until = json.getLong("valid_until");

                // count time remaining of jerry position validity
                intervalTime = (valid_until*1000) - timeNow;

            } catch(Exception e){

            }

            return result;
        }

        @Override
        protected void onPostExecute(String params){
            // show map
            setUpMap();
            // start the timer to count validity
            timer();
        }

    }
}
