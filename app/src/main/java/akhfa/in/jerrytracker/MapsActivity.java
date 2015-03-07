package akhfa.in.jerrytracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //Buat json
    private static final String TAG_LATITUDE = "lat";
    private static final String TAG_LONGITUDE = "long";
    private static final String url= "http://167.205.32.46/pbd/api/track?nim=13513601";
    static boolean jsonBool=false;

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

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsAkhfa))
                    .getMap();
            if (mMap != null) {
                //execute parsing json from server
                mMap.setMyLocationEnabled(true);
                new JSONParse().execute();

            }
        }
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONParser jParser = new JSONParser();

            JSONObject json = jParser.getJSONFromUrl(url);
            if(json==null)
            {
                jsonBool=false;
            }
            else jsonBool=true;
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            if(jsonBool==true)
            {
                try{
                    Log.e("status",jsonBool+"");
                    //simpan di variable
                    String latitude = json.getString(TAG_LATITUDE);
                    String longitude = json.getString(TAG_LONGITUDE);
                    Log.e("lat",latitude);
                    Log.e("lat",longitude);

                    Double lat=Double.parseDouble(latitude.toString());
                    Double longi=Double.parseDouble(longitude.toString());

                    //add marker ke peta
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, longi)).title("location").snippet(""));

                    //Update camera to point the marker
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi), 18.0f));

                }catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
            //Jika tidak ada koneksi atau server down, keluarkan message error
            else {Toast.makeText(getApplicationContext(), "error getting data", Toast.LENGTH_SHORT).show();
                Log.e("status",jsonBool+"");}
        }

    }
}

