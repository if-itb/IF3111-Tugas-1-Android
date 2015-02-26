package adwisatya.jerrytracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {

    private ImageView image;

    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    TextView tvHeading;
    TextView txtLatLong;
    TextView txtTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageViewCompass);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        txtLatLong = (TextView) findViewById(R.id.txtLatLong);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            /* Bagian GPS */
            @Override
            public void onLocationChanged(Location loc){
                loc.getLongitude();
                loc.getLatitude();
                String Text = "Latitude: " +loc.getLatitude() + " " + "Longitude: " + loc.getLongitude();
                Toast.makeText(getApplicationContext(),Text,Toast.LENGTH_SHORT).show();
                txtLatLong.setText(Text);
            }
            @Override
            public void onProviderDisabled(String provider){
                Toast.makeText(getApplicationContext(),"Gps Disabled", Toast.LENGTH_SHORT).show();
                txtTimer.setText("GPS Disabled");

            }
            @Override
            public void onProviderEnabled(String provider){
                Toast.makeText(getApplicationContext(),"Gps Enabled", Toast.LENGTH_SHORT).show();
                txtTimer.setText("GPS Enabled");
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras){

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
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
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }
    public void onSensorChanged(SensorEvent event){
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        RotateAnimation ra = new RotateAnimation(currentDegree,-degree,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}