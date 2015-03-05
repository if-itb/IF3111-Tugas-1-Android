package com.android.mario.getthejerry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

/**
 * Created by Mario on 06/03/2015.
 */
public class Compass extends ImageView{
    Paint paint;
    float direction = 0;

    public Compass(Context context){
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        this.setImageResource(R.drawable.compass);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int height = this.getHeight();
        int width = this.getWidth();

        canvas.rotate(direction, width / 2, height / 2);
        super.onDraw(canvas);
    }

    public void updateData(float direction){
        this.direction = direction;
        invalidate();
    }
}
