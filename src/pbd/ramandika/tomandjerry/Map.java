package pbd.ramandika.tomandjerry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends ActionBarActivity implements SensorEventListener{
	private ProgressDialog pDialog;
	private ImageButton imageButton;
	private ImageView compassView;
	private ImageView mPointer;
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
	
	//URL to get Jerry's Position
	private static String url="http://167.205.32.46/pbd/api/track?nim=13512078";
	
	//JSON Node names
	private static final String TAG_LONGITUDE="long";
	private static final String TAG_LATITUDE="lat";
	private static final String  TAG_VALID="valid_until";
	
    //local variable
	private String longitude=null;
	private String latitude=null;
	private String valid=null;
	private GoogleMap map;
	private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
	
	//Timer Task
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    Date nextService;
    long seconds;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        listenerCompass();
        new GetJSON().execute();
    	//TurnOn Compass
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    mPointer = (ImageView) findViewById(R.id.compassview);
    }
    
	  public void initializeTimerTask() {
		          timerTask = new TimerTask() {
		              public void run() {
		                   
		                  //use a handler to run a toast that shows the current timestamp
		                  handler.post(new Runnable() {
		                      public void run() {
		                    	  new GetJSON().execute();
		                      }
		                  });
		              }
		          };
	  }

	public void startTimer(){
		timer=new Timer();
		initializeTimerTask();
		timer.schedule(timerTask, seconds*1000);
	}
	protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
	}
	 
	protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
	}
	
	protected void onStop(){
		super.onStop();
	    mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
	}
	
	protected void onDestroy() {
	    super.onDestroy();
	}

	public void onSensorChanged(SensorEvent event) {
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
     
            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	protected void listenerCompass(){
		imageButton = (ImageButton) findViewById(R.id.compasslogo);
		 
		imageButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
 
			   /*Toast.makeText(Map.this,
				"ImageButton is clicked!", Toast.LENGTH_SHORT).show();*/
				compassView=(ImageView)findViewById(R.id.compassview);
				float alpha;
				alpha=compassView.getAlpha();
				if(alpha==0.0){
					alpha=1;
				}
				else{
					alpha=0;
				}
				compassView.setAlpha(alpha);
			}
		});
	}
	
    private class GetJSON extends AsyncTask<Void,Void,Void>{
    	@Override
    	protected void onPreExecute(){
    		super.onPreExecute();
    		pDialog=new ProgressDialog(Map.this);
    		pDialog.setMessage("Retrieveing Map....");
    		pDialog.setCancelable(false);
    		pDialog.show();
    	}
    	@Override
    	protected Void doInBackground(Void... arg0){
    		ServiceHandler service=new ServiceHandler();
    		String jsonStr=service.makeServiceCall(url,ServiceHandler.GET);
    		Log.d("Response:",jsonStr);
    		
    		if(jsonStr!=null){
    			try{
    				JSONObject jsonObj=new JSONObject(jsonStr);
    				latitude=jsonObj.getString(TAG_LATITUDE);
    				longitude=jsonObj.getString(TAG_LONGITUDE);
    				valid=jsonObj.getString(TAG_VALID);
    			}catch(JSONException e){
    				e.printStackTrace();
    			}
    		}
    		else{
    			Log.e("ServiceHandler","Couldn't get any data from server");
    		}
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result){
    		super.onPostExecute(result);
    		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
    		if(pDialog.isShowing()) pDialog.dismiss();
    	    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	    map.setMyLocationEnabled(true);
    	    addJerryLocation();
    	    addYourLocation();
    		LatLngBounds.Builder builder = new LatLngBounds.Builder();
    		for (Marker marker : mMarkerArray) {
    		    builder.include(marker.getPosition());
    		}
    		LatLngBounds bounds = builder.build();
    		int padding = 200; // offset from edges of the map in pixels
    		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
    		map.animateCamera(cu);
    		try{
        		Date now=new Date();
    			SimpleDateFormat sfd=new SimpleDateFormat("d MMMM yyyy 'at' h:mm a");
    			String strdate=unixToDate(valid);
    			nextService=sfd.parse(strdate);
        		seconds = (nextService.getTime()-now.getTime())/1000;
        		startTimer();
        		new CountDownTimer(seconds*1000, 1000) {
            		TextView cntdwn=(TextView)findViewById(R.id.countdown);
        		     public void onTick(long millisUntilFinished) {
        		         cntdwn.setText("seconds remaining: " + millisUntilFinished / 1000);
        		     }

        		     public void onFinish() {
        		         cntdwn.setText("done!");
        		     }
        		  }.start();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    private void addJerryLocation(){
	    double lat = Double.parseDouble(latitude);
	    double lon = Double.parseDouble(longitude);
	    LatLng location=new LatLng(lat,lon);
	    Marker marker=map.addMarker(new MarkerOptions().position(location).title("Jerry's Here"));
	    mMarkerArray.add(marker); 
    }
    
    private void addYourLocation(){
    	LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
    	String provider = service.getBestProvider(criteria, false);
    	Location location = service.getLastKnownLocation(provider);
    	LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
	    Marker marker=map.addMarker(new MarkerOptions().position(userLocation).title("You're Here"));
	    mMarkerArray.add(marker);
    }
    

    private String unixToDate(String unix_timestamp) {
        long timestamp = Long.parseLong(unix_timestamp) * 1000;

        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy 'at' h:mm a");
        sdf.setTimeZone(timeZone);
        String date = sdf.format(timestamp);
        
        return date.toString();
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
    
    public void onClick(View view){
		 Intent intent = new Intent(this,Map.class);
		 startActivity(intent); 
    }
}
