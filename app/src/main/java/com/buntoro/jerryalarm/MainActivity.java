package com.buntoro.jerryalarm;


import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.media.tv.TvContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    //URL server GET
    private String urlSpike = "http://167.205.32.46/pbd/api/track?nim=13512038";
    private String urlJerry = "http://167.205.32.46/pbd/api/catch";

    private static String TAG = MainActivity.class.getSimpleName();
    private Button btnSpike, btnJerry, btnToggle;

    //progress dialog
    private ProgressDialog pDialog;

    private TextView txtResponse, txtLocation;

    //temp string
    private String jsonResponse;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    //location updates intervals
    private static int UPDATE_INTERVAL = 10000; //10 sec
    private static int FATEST_INTERVAL = 5000; //5 sec
    private static int DISPLACEMENT = 10; //10 meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSpike = (Button) findViewById(R.id.callSpike);
        btnJerry = (Button) findViewById(R.id.catchJerry);
        btnToggle = (Button) findViewById(R.id.btnToggle);
        txtResponse = (TextView) findViewById(R.id.txtRespone);
        txtLocation = (TextView) findViewById(R.id.txtLocation);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Tunggu sebentar");
        pDialog.setCancelable(false);

        //check play services
        if (checkPlayServices()){
            buildGoogleApiClient();

            createLocationRequest();
        }

        btnSpike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeJsonObjectRequest();
            }
        });
        btnJerry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scanQR();
            }
        });
        btnToggle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                togglePeriodicLocationUpdates();
            }
        });
    }

    private void makeJsonObjectRequest(){
        showpDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, urlSpike, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try{
                    String lat = response.getString("lat");
                    String lon = response.getString("long");
                    String valid_until = response.getString("valid_until");
                    Long valid = Long.parseLong(valid_until, 10);
                    valid *= 1000;
                    Date val = new Date(valid);


                    jsonResponse = "";
                    jsonResponse += "Latitude   : " + lat + "\n\n";
                    jsonResponse += "Longitude  : " + lon + "\n\n";
                    jsonResponse += "valid until: " + val.toString() + "\n\n";

                    txtResponse.setText(jsonResponse);
                }
                catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void scanQR(){
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    private void showpDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog(){
        if(pDialog.isShowing())
            pDialog.dismiss();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                catchJerry(intent.getStringExtra("SCAN_RESULT"));
            }else if(resultCode == RESULT_CANCELED){
                txtResponse.setText("scan gagal");
            }
        }
    }

    private void catchJerry(String token){
        showpDialog();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("nim", "13512038");
        params.put("token", token);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlJerry, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    String msg = response.getString("message");
                    String status = response.getString("code");

                    jsonResponse = "";
                    jsonResponse += "Catch " + msg + "\n\n";
                    jsonResponse += "Status:" + status + "\n\n";

                    txtResponse.setText(jsonResponse);
                }
                catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void togglePeriodicLocationUpdates(){
        if(!mRequestingLocationUpdates){
            btnToggle.setText("STOP");

            mRequestingLocationUpdates = true;

            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started");
        } else {
            btnToggle.setText("START");
            mRequestingLocationUpdates = false;

            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped");
        }
    }

    private void displayLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLastLocation != null){
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            txtLocation.setText(latitude + ", " + longitude);
        } else {
            txtLocation.setText("couldn't get location");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
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

    @Override
    protected void onResume(){
        super.onResume();

        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }
}
