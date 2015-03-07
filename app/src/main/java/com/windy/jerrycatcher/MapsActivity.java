package com.windy.jerrycatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends ActionBarActivity implements SensorEventListener {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView pointer;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private double latitude; // Latitude Jerry
    private double longitude; // Longitude Jerry
    private Long time_countdown;
    private Marker mMarker;

    private TextView mTextViewDays, mTextViewHours, mTextViewMinutes, mTextViewSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getData();
        setUpMapIfNeeded();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ImageView compass = (ImageView) findViewById(R.id.compass);
        pointer = (ImageView) findViewById(R.id.pointer);

        TextView mDays = (TextView) findViewById(R.id.days);
        TextView mHours = (TextView) findViewById(R.id.hours);
        TextView mMinutes = (TextView) findViewById(R.id.minutes);
        TextView mSeconds = (TextView) findViewById(R.id.seconds);

        mTextViewDays = (TextView) findViewById(R.id.textViewDays);
        mTextViewHours = (TextView) findViewById(R.id.textViewHours);
        mTextViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
        mTextViewSeconds = (TextView) findViewById(R.id.textViewSeconds);
    }

    public void setLatitude(double lat) {
        latitude = lat;
    }

    public void setLongitude (double longi) {
        longitude = longi;
    }

    public void getData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13512091";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String temp = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                        Date date_now = null;
                        Long second_now, valid_until;
                        try {
                            date_now = new Date();
                            second_now = date_now.getTime();
                            JSONObject json = new JSONObject(temp);
                            valid_until = json.getLong("valid_until") * 1000;
                            setLatitude(json.getDouble("lat"));
                            setLongitude(json.getDouble("long"));

                            time_countdown = valid_until - second_now;

                            //lokasi jerry
                            LatLng lokasiJerry = new LatLng(latitude, longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiJerry, 15));
                            mMarker = mMap.addMarker(new MarkerOptions().position(lokasiJerry).title("Jerry"));
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.jerry3
                            ));
                            //countdown
                            new CountDownTimer(time_countdown, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    long seconds = millisUntilFinished/1000;
                                    long second = seconds % 60;
                                    long minute = seconds / 60 % 60;
                                    long hour = seconds / 60 / 60 % 24;
                                    long day = seconds / 60 / 60 / 24;

                                    String detik = null;
                                    String menit = null;
                                    String jam = null;
                                    String hari = null;
                                    if (second/10 < 1) {
                                        detik = "0" + second;
                                    } else {
                                        detik = "" + second;
                                    }

                                    if (minute/10 < 1) {
                                        menit = "0" + minute;
                                    } else {
                                        menit = "" + minute;
                                    }

                                    if (hour/10 < 1) {
                                        jam = "0" + hour;
                                    } else {
                                        jam = "" + hour;
                                    }

                                    if (day/10 < 1) {
                                        hari = "0" + day;
                                    } else {
                                        hari = "" + day;
                                    }

                                    mTextViewDays.setText(hari);
                                    mTextViewHours.setText(jam);
                                    mTextViewMinutes.setText(menit);
                                    mTextViewSeconds.setText(detik);
                                }

                                public void onFinish() {
                                    mMarker.remove();
                                    getData();
                                }
                            }.start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }



    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            pointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    /* scan QR Code */

    //product qr code mode
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
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                RequestQueue queue = Volley.newRequestQueue(this);
                String url ="http://167.205.32.46/pbd/api/catch?nim=13512091";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String temp = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                                String message;
                                int code;
                                try {
                                    JSONObject json = new JSONObject(temp);
                                    message = json.getString("message");
                                    code = json.getInt("code");
                                    if (code == 200) {
                                        Toast toast = Toast.makeText(getBaseContext(), "Success!", Toast.LENGTH_LONG);
                                        toast.show();
                                    } else if (code == 400) {
                                        Toast toast = Toast.makeText(getBaseContext(), "MISSING PARAMETER", Toast.LENGTH_LONG);
                                        toast.show();
                                    } else if (code == 403) {
                                        Toast toast = Toast.makeText(getBaseContext(), "FORBIDDEN", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                    }){
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("nim", "13512091");
                        params.put("token", contents);

                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        }
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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
    }
}
