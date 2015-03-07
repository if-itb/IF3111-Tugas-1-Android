package com.example.pbd1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements SensorEventListener {
	GoogleMap googleMap;
	Button button;
	boolean finished = false;
	TextView errorText;
	Date valid_until_date;
	LocationManager myLocation;
	ImageView kompas;
	float sudut;
	final String URL = "http://167.205.32.46/pbd//api/track?nim=13512042";
	final String POST_URL = "http://167.205.32.46/pbd//api/catch";
	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		errorText = (TextView) findViewById(R.id.errorText);
		button = (Button) findViewById(R.id.captureJerry);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("onClickQR", "Berhasil");
				scanQR(v);
			}
		});
		kompas = (ImageView) findViewById(R.id.kompas);
		
		try {
			// Loading map
			initilizeMap();
			(new BackgroundTask()).execute(URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		/*
		(new RefreshThread()).run();
		*/
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */

	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
    protected void onResume() {
		super.onResume();
	    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        finished=true;
        //ngulangin thread
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* Async Task Handling For Requesting New Position of Jerry */
	private class JerryTask extends AsyncTask<String, String, String[]> {

		@Override
		protected String[] doInBackground(String... urls) {
			Log.d("doInBackground", "Berhasil");
			String response = "";
			String result[];
			result = new String[3];
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
						publishProgress("Getting New Information");
					}
					Log.d("Response",response);
					JSONObject object = new JSONObject(response);
					//{"lat":"-6.890323","long":"107.610381","valid_until":1425833999}
					result[0] = (String) object.get("lat");
					result[1] = (String) object.get("long");
					result[2] = (String) object.get("valid_until");
					
				} catch (Exception e) {
					errorText.setText(e.getMessage());
					return null;
				}
			}
			Log.d("Result", result.toString());
			return result;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// latitude and longitude
			Toast.makeText(getBaseContext(), "Yes",Toast.LENGTH_LONG).show();
			if (result != null) {
				
				double latitude = Double.valueOf(result[0]);
				double longitude = Double.valueOf(result[1]);
				valid_until_date = new Date(Long.valueOf(result[2])*1000);

				// create marker
				MarkerOptions marker = new MarkerOptions().position(
						new LatLng(latitude, longitude)).title(
						"Jerry is Here until"+valid_until_date.toString());

				// adding marker
				googleMap.addMarker(marker);
				
			}
		}
		
		 protected void onProgressUpdate(String... onupdate){
	    	 errorText.setText(onupdate[0]);
	     }

	}
	//product qr code mode
	public void scanQR(View v) {
		try {
		 //start the scanning activity from the com.google.zxing.client.android.SCAN intent
		Intent intent = new Intent(ACTION_SCAN);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
		 //on catch, show the download dialog
		Toast.makeText(MainActivity.this, "No Scanner Found", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				//get the extras that are returned from the intent
				String contents = intent.getStringExtra("SCAN_RESULT");
				(new ResultTask()).execute(contents);
			}
		}
	}
	
	private class ResultTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String result = params[0];
			java.net.URL object;
			int HttpResult=0;
			try {
				object = new java.net.URL(POST_URL);

		    	HttpURLConnection con;
				con = (HttpURLConnection) object.openConnection();
				con.setDoOutput(true);

		    	con.setDoInput(true);

		    	con.setRequestProperty("Content-Type", "application/json");

		    	con.setRequestProperty("Accept", "application/json");

		    	con.setRequestMethod("POST");
		    	//Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();  
				Log.d("Lewat","1");
		    	JSONObject post = new JSONObject();
		    	post.put("nim", "13512042");
		    	post.put("token", result);
		    	Log.d("Lewat",post.toString());
		    	OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());

		    	wr.write(post.toString());
		    	wr.flush();

		    	HttpResult =con.getResponseCode(); 
		    	//Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_SHORT).show();
		    	if(HttpResult == 200 ){
		    	  //  OK  
		    		Log.d("Lewat","2");
		    	}else if(HttpResult == 400){
		    	    // Parameter Tidak Lengkap
		    		Log.d("Lewat","3");
		    	}else if(HttpResult == 403){
		    	    // Parameter Salah
		    		Log.d("Lewat","4");
		    	}  
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("Error","1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("Error","2");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("Error","3");
			}

			return String.valueOf(HttpResult);
		}
		
		@Override
		protected void onPostExecute(String result) {
			String respon="Tom";
			if(result.equalsIgnoreCase("0"))
				respon = "Pelaporan gagal karena koneksi Internet";
			if(result.equalsIgnoreCase("200"))
				respon = "Spike menerima laporan, tunggu arahan berikutnya";
			if(result.equalsIgnoreCase("400"))
				respon = "Pelaporan gagal karena laporan tidak lengkap";
			if(result.equalsIgnoreCase("403"))
				respon = "Jerry yang ditangkap salah";
			Toast.makeText(getApplicationContext(),respon, Toast.LENGTH_SHORT).show();
		}
		
		
    	
    }
	
	
	
	public void onBackPressed() {    
	    finished=true;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		 // get the angle around the z-axis rotated
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
     
            kompas.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	private class BackgroundTask extends AsyncTask<String,String,String> {
	     protected String doInBackground(String... urls) {
	         while(!finished){
	        	 if((new Date()).after(valid_until_date)){
	        		 (new JerryTask()).execute(URL);
	        	 }
	        	 else{
	        		 publishProgress("No New Information");
	        	 }
	         }
			return null;
	     }

	     protected void onPostExecute(String result) {
	    	 // do nothing?
	     }
	     
	     protected void onProgressUpdate(String... onupdate){
	    	 errorText.setText(onupdate[0]);
	     }
	 }
	
}
