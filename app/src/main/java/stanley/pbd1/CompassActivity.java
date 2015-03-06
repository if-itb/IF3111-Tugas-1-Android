package stanley.pbd1;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Stanley on 3/4/2015.
 */
public class CompassActivity extends Activity implements SensorEventListener, View.OnClickListener {
    //gambar kompas
    private ImageView image;
    //sudut putar kompas
    private float currentDegree = 0f;
    //Sensor Manager
    private SensorManager sensorm;

    Button back;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        image = (ImageView)findViewById(R.id.compassimage);
        tv = (TextView) findViewById(R.id.tvdegree);
        sensorm = (SensorManager) getSystemService(SENSOR_SERVICE);
        back = (Button) findViewById(R.id.backbutton);
        back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorm.registerListener(this, sensorm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        tv.setText("Sudut : " + Float.toString(degree)+ " derajat");
        RotateAnimation ra = new RotateAnimation(
                currentDegree,-degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        );
        ra.setDuration(600);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree=-degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
        //N/A
    }

    public void backbuttonClick(){
        finish();
    }

    public void onClick (View v){
        switch(v.getId()){
            case R.id.backbutton:
                backbuttonClick();
                break;
        }
    }
}
