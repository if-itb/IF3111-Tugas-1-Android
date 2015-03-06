package com.yanfa.jerrysearcher;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Yanfa on 06/03/2015.
 */
public class FinderDialogFragment extends DialogFragment implements SensorEventListener {

    private ImageView compassView;
    private float currentDegree = 0f;
    private SensorManager sm;
    private Dialog dialog;
    private FrameLayout mainLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.jerry_fragment);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(100));

        mainLayout = (FrameLayout)dialog.findViewById(R.id.mainLayout);
        compassView = (ImageView)dialog.findViewById(R.id.jerryPointer);
        sm = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // for the system's orientation sensor registered listeners
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        return dialog;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

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
        compassView.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
