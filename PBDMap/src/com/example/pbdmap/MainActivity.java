package com.example.pbdmap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements LocationListener,
	GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
	SensorEventListener {

    /* Kompas */
	private ImageView kompasImg;
	private float currentDegree = 0f;
	private SensorManager mSensorManager;
	
	/* Google service */
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    ProgressDialog pDialog;
    
    /* Map & Location*/
    private GoogleMap googleMap;
    String jLat, jLon, jValid;
    LatLng targetLatLng;
    
    /* QR */
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    
    /* Token */
    private static final String NIM = "13512068";
    private static final String url = "http://167.205.32.46/pbd/api/track?nim="+NIM;
    private static final String url2 = "http://167.205.32.46/pbd/api/catch";
    private String token;
    private TextView tokenView;
    
    /* Timer */
    private long time;
    private TextView timeView;
    
    /* Status */
    private boolean catched;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

		mLocationRequest = LocationRequest.create()
		.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
		.setInterval(10 * 1000)        // 10 seconds, in milliseconds
		.setFastestInterval(1 * 1000); // 1 second, in milliseconds
        
        catched = false;
        
        timeView = (TextView) findViewById(R.id.timeTextView);
        time = 0;
        setTimeRemaining(0);
        
        statusView = (TextView) findViewById(R.id.statusTextView);
        setStatus();
        
        tokenView = (TextView) findViewById(R.id.tokenTextView);
        setTokenView();
        
        kompasImg = (ImageView) findViewById(R.id.kompasImg);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        new GetInfo().execute();
        initializeMap();
    }
 
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }
        googleMap.clear();
        if (targetLatLng != null && !catched) {
		    MarkerOptions target = new MarkerOptions()
	        .position(targetLatLng)
	        .title("Lokasi target").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry));
	        googleMap.addMarker(target);
	    }
        googleMap.setMyLocationEnabled(true);
    }
 
    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        googleMap.setMyLocationEnabled(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
        	LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        googleMap.setMyLocationEnabled(false);
    }
    
    private void handleNewLocation(Location location) {
    	double currentLatitude = location.getLatitude();
	    double currentLongitude = location.getLongitude();
	    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
	    Toast.makeText(this, currentLatitude+" "+currentLongitude, Toast.LENGTH_LONG).show();
	    googleMap.clear();
	    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
	    
	    if (targetLatLng != null && !catched) {
		    MarkerOptions target = new MarkerOptions()
	        .position(targetLatLng)
	        .title("Lokasi target").icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry));
	        googleMap.addMarker(target);
	    }
    }
    
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		handleNewLocation(location);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (location == null) {
	        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	    }
	    else {
	        handleNewLocation(location);
	    }
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void refresh(View v) {
		if (time < System.currentTimeMillis())
			new GetInfo().execute();
		catched = false;
		token = null;
		initializeMap();
		setTokenView();
		setStatus();
	}
	
	/** Method-method qr code **/
	public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                token = contents;
                setTokenView();
            }
        }
    }
	
	private void setTimeRemaining(long sisaDetik) {
		String tm = "Sisa waktu token : ";
		int time;
		if (sisaDetik > 86400) {	//hari
			time = (int)sisaDetik / 86400;
			tm += time + " hari ";
			sisaDetik %= 86400;
		}
		if(sisaDetik > 3600) {		//jam
			time = (int)sisaDetik / 3600;
			tm += time + " jam ";
			sisaDetik %= 3600;
		}
		if(sisaDetik > 60) {
			time = (int)sisaDetik / 60;
			tm += time + " menit ";
			sisaDetik %= 60;
		}
		tm += sisaDetik + " detik";
		timeView.setText(tm);
	}
	
	private void setStatus() {
		if (catched) {
			statusView.setTextColor(Color.parseColor("#32CD32"));		// Hijau
			statusView.setText("Status Jerry : Sudah ditangkap");
			
		} else {
			statusView.setTextColor(Color.parseColor("#FF0000"));	// Merah
			statusView.setText("Status Jerry : Belum ditangkap");
		}
	}
	
	private void setTokenView() {
		if (token == null)
			tokenView.setText("#Token");
		else
			tokenView.setText(token);
	}
	
	/* Post */
	
	public void postToken(View v) {
        new PostInfo().execute();
    }
	
	//////////////////////////////////////////
	private class GetInfo extends AsyncTask<Void, Void, Void> {
		 
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            // Showing progress dialog
	            pDialog = new ProgressDialog(MainActivity.this);
	            pDialog.setMessage("Harap tunggu...");
	            pDialog.setCancelable(false);
	            pDialog.show();
	        }
	 
	        @Override
	        protected Void doInBackground(Void... arg0) {
	            // Creating service handler class instance
	            ServiceHandler sh = new ServiceHandler();
	 
	            // Making a request to url and getting response
	            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
	            String s = null;
	            try {
					s = new String(jsonStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            
	            Log.d("Response: ", "> " + jsonStr);
	 
	            if (s != null) {
	                try {
	                    JSONObject jsonObj = new JSONObject(s);
	                    jLat = jsonObj.getString("lat");
	                    jLon = jsonObj.getString("long");
	                    jValid = jsonObj.getString("valid_until");
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	            } else {
	                Log.e("ServiceHandler", "Couldn't get any data from the url");
	            }
	 
	            return null;
	        }
	 
	        @Override
	        protected void onPostExecute(Void result) {
	            super.onPostExecute(result);
	            // Dismiss the progress dialog
	            if (pDialog.isShowing())
	                pDialog.dismiss();
	            double lat = Double.parseDouble(jLat);
                double lon = Double.parseDouble(jLon);
                targetLatLng = new LatLng(lat, lon);

                time = Long.parseLong(jValid)*1000;
//                time = System.currentTimeMillis() + 5000;
                new CountDownTimer( (time - System.currentTimeMillis()), 1000) {
                	public void onTick(long milisUntilFinished) {
                		long sisa = (time - System.currentTimeMillis())/1000;
                		if(sisa < 0)
                			setTimeRemaining(0);
                		else
                			setTimeRemaining(sisa);
                	}
                	
                	public void onFinish() {
                		time = 0;
                		setTimeRemaining(0);
                		Toast.makeText(MainActivity.this, "Waktu Habis!", Toast.LENGTH_LONG).show();
                		new GetInfo().execute();
                		initializeMap();
                	}
                	
                }.start();
	        }
	 
	    }
	
	
	private class PostInfo extends AsyncTask<Void, Void, Void> {
		 
		private String response;
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Harap tunggu...");
            pDialog.setCancelable(false);
            pDialog.show();
 
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
 
            // Making a request to url and getting response
            List<NameValuePair> nvp = new ArrayList<NameValuePair>();
            nvp.add(new BasicNameValuePair("nim", NIM));
            nvp.add(new BasicNameValuePair("token", token));
            String jsonStr = sh.makeServiceCall(url2, ServiceHandler.POST, nvp);
            try {
				response = new String(jsonStr.getBytes("ISO-8859-1"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
//            new GetInfo().execute();
//            initializeMap();
            try {
				JSONObject obj = new JSONObject(response);
				String code = obj.getString("code");
				if (Integer.parseInt(code) == 200) {
					catched = true;
					setStatus();
					googleMap.clear();
				} else {
					if (time < System.currentTimeMillis())
						new GetInfo().execute();
		            initializeMap();					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
    }


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

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
        kompasImg.startAnimation(ra);
        currentDegree = -degree;
	}
}

