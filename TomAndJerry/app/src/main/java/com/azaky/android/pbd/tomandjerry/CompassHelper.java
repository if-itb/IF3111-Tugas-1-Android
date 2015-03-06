package com.azaky.android.pbd.tomandjerry;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Toshiba on 3/4/2015.
 *
 * Class that simulates compass by listening to magnetic sensor
 */
public class CompassHelper implements SensorEventListener {
    private SensorManager mSensorManager;
    private Context mContext;
    private View mCompassImage;
    private float currentDegree = 0.f;

    public CompassHelper(Context context, View view) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mCompassImage = view;
    }

    public void registerListener() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterListener() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        mCompassImage.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    public SensorManager getSensorManager() {
        return mSensorManager;
    }

    public void setSensorManager(SensorManager mSensorManager) {
        this.mSensorManager = mSensorManager;
    }
}
