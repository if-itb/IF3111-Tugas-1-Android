package wxr.tugas1android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    public double LatVal = -6, LongVal = 100;
    public long theTime;
    boolean lock = true;
    public String time = "Undefined time";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try {
            new Task().execute(getApplicationContext()).get();
            CountDown((1000 * theTime) - System.currentTimeMillis());
            setUpMapIfNeeded();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

    private void setUpMap() {
        Bundle B = getIntent().getExtras();
        mMap.addMarker(new MarkerOptions().position(new LatLng(LatVal, LongVal)).title("Jerry"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LatVal, LongVal), 18));
    }

    private void CountDown(long theTimeDif){

        new CountDownTimer(theTimeDif, 1000) {
            private TextView mTextField = (TextView) findViewById(R.id.textView);

            public void onTick(long millisUntilFinished) {
                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTextField.setText("Refreshing!");
                try {
                    new Task().execute(getApplicationContext()).get();
                    setUpMap();
                    CountDown((theTime * 1000) - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public class Task extends AsyncTask<Context, String, String> {
        private Context context;
        public JSONObject getData(String url) {
            JSONObject theData = new JSONObject();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response;
            String result = "";
            try {
                response = client.execute(request);
                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF8"));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                Log.d("api/track", result);

                String resulte = result.substring(result.indexOf("{"), (result.lastIndexOf("}") + 1));
                Log.d("api/track", resulte);
                theData = new JSONObject(resulte);
            } catch (Exception e) {
            }

            //Toast T = new Toast(getApplicationContext());
            //T.setText(result);

            //Toast.makeText(getApplication().getApplicationContext(), result, Toast.LENGTH_LONG).show();
            return theData;
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];
            JSONObject theData = getData("http://167.205.32.46/pbd/api/track?nim=13512039");
            try {
                LatVal = theData.getDouble("lat");
                LongVal = theData.getDouble("long");
                theTime = theData.getLong("valid_until");

                time = String.format("%02d:%02d:%02d", ((TimeUnit.SECONDS.toHours(theTime) + 7) % 24),
                        TimeUnit.SECONDS.toMinutes(theTime) % 60,
                        TimeUnit.SECONDS.toSeconds(theTime) % 60);

            }
            catch(JSONException e){}
            lock = false;

            return new String();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplication().getApplicationContext(), "Finished loading from PBD Server", Toast.LENGTH_LONG).show();

        }
    }


}
