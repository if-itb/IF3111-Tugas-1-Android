package com.coderbodoh.tomandjerryapp;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity {

    final String ENDPOINT = "http://167.205.32.46/pbd";
    private LatLng targetPosition = new LatLng(-6.890323,107.610381);
    private Long validUntil = -1L;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker markerJerry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        if(isConnected()){
            new HttpAsyncTask().execute(ENDPOINT + "/api/track?nim=13512007");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

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
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetPosition));
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            double lat = targetPosition.latitude;
            double lng = targetPosition.longitude;
            try {
                JSONObject jObject = new JSONObject(result);
                Iterator<?> keys = jObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = jObject.getString(key);
                    if (key.equals("lat")){
                        lat = Double.parseDouble(value);
                    } else if (key.equals("long")){
                        lng = Double.parseDouble(value);
                    } else if (key.equals("valid_until")){
                        validUntil = Long.parseLong(value);
                    }
                }
                targetPosition = new LatLng(lat,lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(targetPosition));
                markerJerry =  mMap.addMarker(new MarkerOptions().position(targetPosition).title("Posisi Jerry"));
                //markerJerry.setPosition(targetPosition);

                Toast.makeText(getBaseContext(), "Jerry Found!\nlattitude = "+ targetPosition.latitude +"\nlongitude = "+ targetPosition.longitude+"\n", Toast.LENGTH_LONG).show();

                Log.d("lat","new lattitude = " + Double.toString(markerJerry.getPosition().latitude));
                Log.d("lng","new longitude = " + Double.toString(markerJerry.getPosition().longitude));
                validUntil = (System.currentTimeMillis()/1000) + 10;
            } catch (JSONException je) {
                je.printStackTrace();
                Toast.makeText(getApplicationContext(), "JSONException:  " + je.getMessage(), Toast.LENGTH_LONG).show();
                //set to default location
            }
        }
    }

}
