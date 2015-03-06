package id.web.trapholeproductions.tom;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {

    Handler mHandler = new Handler();
    long timeLeft = -1;
    boolean stopThread = true;

    private final int STOP_THREAD = 1;
    private final String DEBUG_TAG = "Tom&JerryDebug";

    public void askSpike(View view) {

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://167.205.32.46/pbd/api/track?nim=13510052";

// Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        final TextView lat_text = (TextView) findViewById(R.id.lat_text);
                        final TextView long_text = (TextView) findViewById(R.id.long_text);
                        final TextView time_text = (TextView) findViewById(R.id.time_text);
                        MapFragment mapFragment = (MapFragment) getFragmentManager()
                                .findFragmentById(R.id.map);
                        GoogleMap map = mapFragment.getMap();

                        try {
                            double lat = Double.parseDouble(response.getString("lat"));
                            double longit = Double.parseDouble(response.getString("long"));
                            long validUntil = Long.parseLong(response.getString("valid_until"));

                            timeLeft = validUntil - (System.currentTimeMillis()/1000);
                            Calendar deadline = Calendar.getInstance();
                            deadline.setTimeInMillis(timeLeft*1000);
                            String timeLeft_text = "Time left until Jerry moves: ";
                            if (deadline.get(Calendar.MINUTE) > 0) {
                                timeLeft_text += Integer.toString(deadline.get(Calendar.MINUTE)) + " minutes ";
                            }
                            timeLeft_text += Integer.toString(deadline.get(Calendar.SECOND)) + " seconds.";

                            lat_text.setText("Latitude: " + Double.toString(lat));
                            long_text.setText("Longitude: " + Double.toString(longit));
                            time_text.setText(timeLeft_text);

                            map.addMarker(new MarkerOptions().position(new LatLng(lat, longit)).title("Jerry"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        stopThread = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final TextView lat_text = (TextView) findViewById(R.id.lat_text);
                lat_text.setText("Error in parsing JSON.");
            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setMyLocationEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: gimana cara menghentikannya??
                while (!stopThread) {
                    try {
                        Log.d(DEBUG_TAG, "Ext. thread running.");
                        Thread.sleep(1000);
                        if (timeLeft > -1) {
                            timeLeft--;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Calendar deadline = Calendar.getInstance();
                                    deadline.setTimeInMillis(timeLeft*1000);
                                    String timeLeft_text = "Time left until Jerry moves: ";
                                    if (deadline.get(Calendar.MINUTE) > 0) {
                                        timeLeft_text += Integer.toString(deadline.get(Calendar.MINUTE)) + " minutes ";
                                    }
                                    timeLeft_text += Integer.toString(deadline.get(Calendar.SECOND)) + " seconds.";
                                    final TextView time_text = (TextView) findViewById(R.id.time_text);
                                    time_text.setText(timeLeft_text);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopThread = true;
        Log.d(DEBUG_TAG, "Ext. thread stopped.");
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
