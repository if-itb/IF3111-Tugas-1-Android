package com.example.tomandjerry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class GPS extends Activity implements LocationListener{
	
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;
	public String lat;
	public String provider;
	protected String latitude,longitude; 
	protected boolean gps_enabled,network_enabled;
	double targetlat;
	double targetlong;
	double currentLat;
	double currentLong;
	
	int validuntil;
	
	
	Location location;
	MapFragment mapFragment;
    GoogleMap map;
    Marker targetMarker;
    Marker currentMarker;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

		
		mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
		map = mapFragment.getMap();
	    LatLng current = new LatLng(currentLat, currentLong);
	    LatLng target = new LatLng(targetlat, targetlong);
	    currentMarker = map.addMarker(new MarkerOptions()
		    .title("Posisimu")
		    .position(current));
	    
	    targetMarker = map.addMarker(new MarkerOptions()
		    .title("Jerry")
		    .position(target));
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 13));
		
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask task = new TimerTask(){
			@Override
			public void run(){
				handler.post(new Runnable(){
					public void run(){
						GetPositionAsyncTask task = new GetPositionAsyncTask();
						task.execute();
						LatLng target = new LatLng(targetlat, targetlong);
						targetMarker.setPosition(target);
					}
				});
			}
		};
		timer.schedule(task, 0, 10000);
	}
	
	class GetPositionAsyncTask extends AsyncTask<String, String, Void> {

	    InputStream inputStream = null;
	    String result = ""; 

	    protected void onPreExecute() {

	    }

	    @Override
	    protected Void doInBackground(String... params) {

	        String url_select = "http://167.205.32.46/pbd/api/track?nim=13512056";

	        try {
	            // Set up HTTP post

	            // HttpClient is more then less deprecated. Need to change to URLConnection
	            HttpClient httpClient = new DefaultHttpClient();

	            HttpGet httpGet = new HttpGet(url_select);
	            HttpResponse httpResponse = httpClient.execute(httpGet);
	            HttpEntity httpEntity = httpResponse.getEntity();

	            // Read content & Log
	            inputStream = httpEntity.getContent();
	        } catch (UnsupportedEncodingException e1) {
	            Log.e("UnsupportedEncodingException", e1.toString());
	            e1.printStackTrace();
	        } catch (ClientProtocolException e2) {
	            Log.e("ClientProtocolException", e2.toString());
	            e2.printStackTrace();
	        } catch (IllegalStateException e3) {
	            Log.e("IllegalStateException", e3.toString());
	            e3.printStackTrace();
	        } catch (IOException e4) {
	            Log.e("IOException", e4.toString());
	            e4.printStackTrace();
	        }
	        // Convert response to string using String Builder
	        try {
	            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
	            StringBuilder sBuilder = new StringBuilder();

	            String line = null;
	            while ((line = bReader.readLine()) != null) {
	                sBuilder.append(line + "\n");
	            }

	            inputStream.close();
	            result = sBuilder.toString();

	        } catch (Exception e) {
	            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
	        }
			return null;
	    } // protected Void doInBackground(String... params)
	    
	    protected void onPostExecute(Void v) {
	        //parse JSON data
	        try {

                JSONObject jObject = new JSONObject(result);

                GPS.this.targetlat = jObject.getDouble("lat");
                GPS.this.targetlong = jObject.getDouble("long");
                GPS.this.validuntil = jObject.getInt("valid_until");
              

	        } catch (JSONException e) {
	            Log.e("JSONException", "Error: " + e.toString());
	        } // catch (JSONException e)
	    } // protected void onPostExecute(Void v)
	} //class MyAsyncTask extends AsyncTask<String, String, Void>

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		currentLat = location.getLatitude();
		currentLong = location.getLongitude();
		LatLng current = new LatLng(currentLat, currentLong);
        currentMarker.setPosition(current);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
