package com.sisters.khorosho;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class Compass extends ActionBarActivity implements SensorListener {

    SensorManager sensorManager;
    static final int sensor = SensorManager.SENSOR_ORIENTATION;
    CompassPic compassPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassPic = new CompassPic(this);

        setContentView(compassPic);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor != Compass.sensor)
            return;
        int orientation = (int) values[0];
        compassPic.setDirection(orientation);
    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }
}

class CompassPic extends ImageView {
    Paint paint;
    int direction = 0;

    public CompassPic(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        this.setImageResource(R.drawable.compass);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int height = this.getHeight();
        int width = this.getWidth();

        canvas.rotate(direction, width / 2, height / 2);
        super.onDraw(canvas);
    }

    public void setDirection(int direction) {
        this.direction = direction;
        this.invalidate();
    }
}