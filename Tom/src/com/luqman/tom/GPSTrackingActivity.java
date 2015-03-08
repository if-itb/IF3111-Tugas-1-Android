package com.luqman.tom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class GPSTrackingActivity extends Activity  {
	 double targetLatitude = 0;
     double targetLongitude = 0;
     int validUntil;
     LocationManager locationManager;
     Location location;
     
     MapFragment mapFragment;
     GoogleMap map;
     Marker targetMarker;
     Marker currentMarker;
     final LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		        double longitude = location.getLongitude();
		        double latitude = location.getLatitude();
		        LatLng current = new LatLng(latitude, longitude);
		        currentMarker.setPosition(current);
		    }

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			};
		};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpstracking);
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
		map = mapFragment.getMap();
	    double longitude = location.getLongitude();
	    double latitude = location.getLatitude();
	    LatLng current = new LatLng(latitude, longitude);
	    LatLng target = new LatLng(targetLatitude, targetLongitude);
	    BitmapDescriptor iconTarget = BitmapDescriptorFactory.fromResource(R.drawable.marker_jerry);
	    BitmapDescriptor iconCurrent = BitmapDescriptorFactory.fromResource(R.drawable.marker_tom);
	    currentMarker = map.addMarker(new MarkerOptions()
		    .title("Posisimu")
		    .icon(iconCurrent)
		    .position(current));
	    
	    targetMarker = map.addMarker(new MarkerOptions()
		    .title("Jerry")
		    .icon(iconTarget)
		    .position(target));
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 13));
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {       
		     @Override
		     public void run() {
		       handler.post(new Runnable() {
		          public void run() {  
		        	  GetPositionAsync task = new GetPositionAsync();
		      	      task.execute();
		        	  LatLng target = new LatLng(targetLatitude, targetLongitude);
		        	  targetMarker.setPosition(target);
		          }
		        });
		      }
		};
		timer.schedule(task, 0, 10000);
		

	}
	
	
	public class GetPositionAsync extends AsyncTask<String, String, Void>  {
		// private ProgressDialog progressDialog = new ProgressDialog(GPSTrackingActivity.this);
		 
		    InputStream inputStream = null;
		    String result = ""; 

		    protected void onPreExecute() {
		       // progressDialog.setMessage("Downloading your data...");
		       // progressDialog.show();
		       // progressDialog.setOnCancelListener(new OnCancelListener() {
				//	@Override
				//	public void onCancel(DialogInterface dialog) {
				//		GetPositionAsync.this.cancel(true);
				//	}
		       // });
		    }
		    
		    @Override
		    protected Void doInBackground(String... params) {

		        String url_select = "http://167.205.32.46/pbd/api/track?nim=13512054";


		        try {
		            HttpClient httpClient = new DefaultHttpClient();

		            HttpGet httpGet = new HttpGet(url_select);
		            HttpResponse httpResponse = httpClient.execute(httpGet);
		            HttpEntity httpEntity = httpResponse.getEntity();

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
		        //	this.progressDialog.setMessage(result);

	                JSONObject jObject = new JSONObject(result);

	                GPSTrackingActivity.this.targetLatitude = jObject.getDouble("lat");
	                GPSTrackingActivity.this.targetLongitude = jObject.getDouble("long");
	                GPSTrackingActivity.this.validUntil = jObject.getInt("valid_until");
	               // GPSTrackingActivity.this.tvLatitude.setText(String.valueOf(GPSTrackingActivity.this.targetLatitude));
	               // GPSTrackingActivity.this.tvLongitude.setText(String.valueOf(GPSTrackingActivity.this.targetLongitude));
		           // this.progressDialog.dismiss();
		        } catch (JSONException e) {
		            Log.e("JSONException", "Error: " + e.toString());
		        } // catch (JSONException e)
		    } // protected void onPostExecute(Void v)
	}
}
