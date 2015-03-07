package id.web.trapholeproductions.tom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, SensorEventListener {

    Handler mHandler = new Handler();

    double latitude = Double.NaN, longitude = Double.NaN;
    long timeLeft = -1;
    RequestQueue queue;
    int catchResponseCode;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    private final String DEBUG_TAG = "Tom&JerryDebug";
    private final String TRACK_URL = "http://167.205.32.46/pbd/api/track?nim=13510052";
    private final String CATCH_URL = "http://167.205.32.46/pbd/api/catch";
    private final LatLng POSISI_KOLAM_INTEL = new LatLng(-6.890323, 107.610381);

    Runnable rticker = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.d(DEBUG_TAG, "ticker run");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                if (timeLeft > -1) {
                    timeLeft--;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final TextView time_text = (TextView) findViewById(R.id.time_text);
                            time_text.setText(timeLeftText());
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final TextView time_text = (TextView) findViewById(R.id.time_text);
                            time_text.setText("No time left.");
                        }
                    });
                }
                if (Thread.interrupted()) return;
            }
        }
    };
    Thread ticker;

    Runnable rpoller = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.d(DEBUG_TAG, "poller run");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final TextView lat_text = (TextView) findViewById(R.id.lat_text);
                        final TextView long_text = (TextView) findViewById(R.id.long_text);
                        lat_text.setText("Updating...");
                        long_text.setText("Updating...");
                        askSpike();
                    }
                });
                if (Thread.interrupted()) return;
            }
        }
    };
    Thread poller;

    public void askSpike() {
        queue.add(new JsonObjectRequest(TRACK_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        final TextView lat_text = (TextView) findViewById(R.id.lat_text);
                        final TextView long_text = (TextView) findViewById(R.id.long_text);
                        final TextView time_text = (TextView) findViewById(R.id.time_text);
                        MapFragment mapFragment = (MapFragment) getFragmentManager()
                                .findFragmentById(R.id.map);
                        if (mapFragment != null) {
                            GoogleMap map = mapFragment.getMap();

                            try {
                                long validUntil = Long.parseLong(response.getString("valid_until"));
                                timeLeft = validUntil - (System.currentTimeMillis() / 1000);
                                time_text.setText(timeLeftText());

                                double new_latitude = Double.parseDouble(response.getString("lat"));
                                double new_longitude = Double.parseDouble(response.getString("long"));

                                if (latitude != new_latitude || longitude != new_longitude) {
                                    latitude = new_latitude;
                                    longitude = new_longitude;

                                    map.clear();
                                    map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry"));
                                }

                                lat_text.setText("Latitude: " + Double.toString(latitude));
                                long_text.setText("Longitude: " + Double.toString(longitude));
                            } catch (JSONException e) {
                                throwParseError();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                throwParseError();
            }
        }));
    }

    public void initiateScan(View view) {
        IntentIntegrator integ = new IntentIntegrator(this);
        integ.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String token = scanResult.getContents();
            JSONObject params = new JSONObject();
            try {
                params.put("nim", "13510052");
                params.put("token", token);
            } catch (JSONException e) {
                throwParseError();
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, CATCH_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (catchResponseCode == 200) {
                            showDialogBox("Token accepted.");
                        } else if (catchResponseCode == 400) {
                            showDialogBox("Missing parameter.");
                        } else if (catchResponseCode == 403) {
                            showDialogBox("Wrong parameter.");
                        } else {
                            showDialogBox("Unknown error.");
                        }
                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (catchResponseCode == 400) {
                        showDialogBox("Missing parameter.");
                    } else if (catchResponseCode == 403) {
                        showDialogBox("Wrong parameter.");
                    } else {
                        showDialogBox("Unknown error.");
                    }
                }
            }){
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    catchResponseCode = response.statusCode;
                    return super.parseNetworkResponse(response);
                }
            };
            queue.add(request);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(POSISI_KOLAM_INTEL, 18.0f));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = Volley.newRequestQueue(this);

        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        askSpike();
        ticker = new Thread(rticker);
        ticker.start();
        poller = new Thread(rpoller);
        poller.start();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticker.interrupt();
        poller.interrupt();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

//        Log.d(DEBUG_TAG, Float.toString(degree));

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
        // not used
    }

    private String timeLeftText() {
        Calendar deadline = Calendar.getInstance();
        deadline.setTimeInMillis(timeLeft * 1000);
        String timeLeft_text = "Time left until Jerry moves: ";
        if (deadline.get(Calendar.MINUTE) > 0) {
            timeLeft_text += Integer.toString(deadline.get(Calendar.MINUTE)) + " minutes ";
        }
        timeLeft_text += Integer.toString(deadline.get(Calendar.SECOND)) + " seconds.";
        return timeLeft_text;
    }

    private void throwParseError() {
        Toast.makeText(this, "JSON parse error.", Toast.LENGTH_LONG);
    }

    private void showDialogBox(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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