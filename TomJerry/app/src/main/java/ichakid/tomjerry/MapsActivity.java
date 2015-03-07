package ichakid.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;
import android.os.Handler;

public class MapsActivity extends FragmentActivity implements LocationListener, SensorEventListener {
    private LatLng myPosition;
    private LatLng JERRY;                   //lokasi Jerry
    private Marker me;
    private Marker jerry;
    private GoogleMap mMap;
    private ImageView imgCompass;           //Gambar compass
    private float currentDegree = 0f;       //variabel untuk menyimpan sudut putaran gambar compass
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] orientation = new float[3];
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private long time = 0;
    private TextView textCounter;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try {
            setUpMap();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Memindahkan kamera langsung ke myPosition dengan zoom 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 17));
        //Zoom in, animasi kameranya
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

        imgCompass = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        textCounter = (TextView) findViewById(R.id.textCounter);
        textCounter.setText("Jerry will be gone in " + String.valueOf(time));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setUpMap();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this, accelerometer);
        mSensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(mR, orientation);
            float azimuthInRadians = orientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(250);
            ra.setFillAfter(true);
            imgCompass.startAnimation(ra);
            currentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onLocationChanged(Location location) {
        TextView tvLocation = (TextView) findViewById(R.id.tv_location);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        myPosition = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        tvLocation.setText("Latitude:" +  latitude  + ", Longitude:"+ longitude );
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() throws ExecutionException, InterruptedException {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mMap = fm.getMap();
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    myPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    onLocationChanged(location);
                }
                locationManager.requestLocationUpdates(provider, 20000, 0, this);
                me = mMap.addMarker(new MarkerOptions()
                        .position(myPosition)
                        .title("Me")
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.tom)));
                getJerryPosition();
                jerry = mMap.addMarker(new MarkerOptions()
                        .position(JERRY)
                        .title("Jerry")
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.jerry)));
            }
        }
    }

    public void getJerryPosition() throws ExecutionException, InterruptedException {
        RequestTask req = new RequestTask();
        req.execute("http://167.205.32.46/pbd/api/track?nim=13512084");
        String response = (String) req.get();
        try {
            JSONObject json = new JSONObject(response);
            String lat = json.getString("lat");
            String lon = json.getString("long");
            String valid_until = json.getString("valid_until");
            time = Long.parseLong(valid_until) * 1000;
            JERRY = new LatLng(Float.parseFloat(lat), Float.parseFloat(lon));
            new CountDownTimer((time - System.currentTimeMillis()), 1000){
                public void onTick(long millisUntilFinished){
                    long cur = (time - System.currentTimeMillis())/1000;
                    textCounter.setText("Jerry will be gone in " + cur / 3600 + ":" + cur % 3600 / 60 + ":" + cur % 3600 % 60 );
                }
                public void onFinish() {
                    time = 0;
                    try {
                        getJerryPosition();
                        animateMarker(JERRY);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void scanQR(View v){
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e){
            showDialog(MapsActivity.this, "No Scanner Found", "Download a QR Code scanner app?", "Yes", "No").show();
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
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                final Context context = this;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Get Token");
                alertDialogBuilder
                        .setMessage("Token: " + contents)
                        .setCancelable(false)
                        .setPositiveButton("Report to Spike",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    makePostRequest(contents);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Exit to Map",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    public void makePostRequest(String token) throws ExecutionException, InterruptedException, JSONException {
        PostTask pt = new PostTask();
        pt.execute("http://167.205.32.46/pbd/api/catch", token);
        String response = pt.get().toString();
        JSONObject json = new JSONObject(response);
        int status = json.getInt("code");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String title = "";
        String message = json.getString("message");
        switch(status){
            case 200 : title = "Congratulations!"; message += "\nYou are awesome."; break;
            case 400 : title = "Oh no!"; message += "\nSorry, you should try again."; break;
            case 403 : title = "Oh no!"; message += "\nSorry, you should try again."; break;
        }
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        if(!this.isFinishing()){
            alertDialog.show();
        }
    }

    public void animateMarker(final LatLng toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(jerry.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final LinearInterpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                jerry.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    jerry.setVisible(true);
                }
            }
        });
    }

    public void viewOnMe(View v){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    public void viewOnJerry(View v){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(JERRY));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }
}
