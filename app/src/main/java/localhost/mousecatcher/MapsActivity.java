package localhost.mousecatcher;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.Request;
import util.Response;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        public void run() {
            new RetrieveFeedTask().execute((Void)null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        handler.post(r);
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void scanQR (View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.d("", contents +" " + format);
                //Encoding POST data
                class OneShotTask implements Runnable {
                    String str;
                    OneShotTask(String s) { str = s; }
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");
                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                        nameValuePair.add(new BasicNameValuePair("nim", "13512066"));
                        nameValuePair.add(new BasicNameValuePair("token", str));
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                        } catch (UnsupportedEncodingException e)
                        {
                            e.printStackTrace();
                        }
                        try {
                            HttpResponse response = httpClient.execute(httpPost);
                            // write response to log
                            Log.d("Http Post Response:", String.valueOf(response.getStatusLine().getStatusCode()));
                        } catch (ClientProtocolException e) {
                            // Log exception
                            e.printStackTrace();
                        } catch (IOException e) {
                            // Log exception
                            e.printStackTrace();
                        }
                    }
                }
                new Thread(new OneShotTask(contents)).start();
            }
        }
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;
        private double lat;
        private double lon;
        private long time;

        protected String doInBackground(Void... v) {
            URL url = null;
            try {
                url = new URL("http://167.205.32.46/pbd/api/track?nim=13512066");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL Mismatch");
            }
            // Get the response (here the current thread will block until response is returned).
            try {
                BufferedReader in;
                InputStreamReader ir = new InputStreamReader(url.openStream());
                in = new BufferedReader(ir);

                String inputLine = in.readLine();
                JSONObject obj = new JSONObject(inputLine);

                lat = obj.getDouble("lat");
                lon = obj.getDouble("long");
                time = obj.getLong("valid_until");
                //mMap.addMarker(new MarkerOptions()
                //                    .position(new LatLng(obj.getDouble("lat"), obj.getDouble("long")))
                //                    .title("Jerry Position"));
                // TODO use validUntil property
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Response acquring interupted");
            }
            return null;
        }

        protected void onPostExecute(String s) {
            // TODO: check this.exception
            // TODO: do something with the feed
            mMap.clear();
            Marker m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title("Jerry Position"));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(m.getPosition());

            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
            handler.postDelayed(r,time - System.currentTimeMillis());
        }
    }
}
