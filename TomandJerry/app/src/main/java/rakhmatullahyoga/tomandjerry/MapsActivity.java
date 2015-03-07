package rakhmatullahyoga.tomandjerry;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity {
    /* Map attributes */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng jerryPosition;
    private LatLng tomPosition;
    private String provider;

    /* JSON attribute */
    private static final String url = "http://167.205.32.46/pbd/api/track?nim=13512053";
    private JSONObject positions = null;
    private static final String TAG_LATITUDE = "lat";
    private static final String TAG_LONGITUDE = "long";
    private static final String TAG_VALIDITY = "valid_until";
    private double latitude;
    private double longitude;
    private int utcTime;
    private boolean ready = false;

    /* Nested class to get Jerry position */
    private class GetPosition extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pDialog;

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
        protected Void doInBackground(Void... params) {
            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            String lat_position = "";
            String long_position = "";
            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr.substring(3));
                    lat_position = jsonObj.getString(TAG_LATITUDE);
                    long_position = jsonObj.getString(TAG_LONGITUDE);
                    utcTime = Integer.parseInt(jsonObj.getString(TAG_VALIDITY));
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.e("Service handler", "Couldn't get any data from url");
            }
            Log.d("stringposition :", "latitude="+lat_position+", longitude="+long_position);
            latitude = Double.parseDouble(lat_position);
            longitude = Double.parseDouble(long_position);
            ready = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
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
            else {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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
        new GetPosition().execute();
        while(!ready);
        Log.d("markerposition :", "latitude="+latitude+", longitude="+longitude);
        jerryPosition = new LatLng(latitude, longitude);
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(jerryPosition).title("Jerry"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jerryPosition, 15));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 3000, null);
    }
}
