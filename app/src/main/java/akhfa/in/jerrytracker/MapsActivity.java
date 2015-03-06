package akhfa.in.jerrytracker;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //Buat json
    private static final String TAG_TOILETS = "toilets";
    private static final String TAG_NAME = "name";
    private static final String TAG_TOILET_ID = "toilet_id";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String url= "http://deaven.bl.ee/data.php";
    static boolean jsonBool=false;
    JSONArray toilets = null;
    static double lat,lon;


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
                Log.e("Sukses","sukses");
                //Menampilkan tombol my location pada peta
                mMap.setMyLocationEnabled(true);
                //Menampilkan marker
                new JSONParse().execute();
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

        mMap.addMarker(new MarkerOptions().position(new LatLng(6, 7)).title("Marker"));
    }


    private class JSONParse extends AsyncTask<String, String, JSONObject> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
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
                    //mengambil array toilets
                    toilets = json.getJSONArray(TAG_TOILETS);
                    //loop pada toilets
                    for(int i=0; i<toilets.length();i++)
                    {
                        JSONObject a = toilets.getJSONObject(i);
                        //simpan di variable
                        String toilet_id = a.getString(TAG_TOILET_ID);
                        String name = a.getString(TAG_NAME);
                        //String type = a.getString(TAG_TYPE);
                        String latitude = a.getString(TAG_LATITUDE);
                        String longitude = a.getString(TAG_LONGITUDE);
                        Log.e("name",name);
                        //konversi data dari String ke double
                        //data dari web service bertipe string
                        Double lat=Double.parseDouble(latitude.toString());
                        Double longi=Double.parseDouble(longitude.toString());
                        //add marker ke peta
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, longi)).title(name).snippet(""));
                    }
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

