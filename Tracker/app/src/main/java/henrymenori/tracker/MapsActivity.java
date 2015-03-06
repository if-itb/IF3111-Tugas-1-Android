package henrymenori.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URI;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements SensorEventListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // attribute for compass
    private ImageView img; // compass picture
    private float degree = 0f; // angle for compass picture
    private SensorManager sensor; // device sensor

    TextView head;

    // attribute for scan QR code
    static final String actionScan = "com.google.zxing.client.android.SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        // compass initialization
        img = (ImageView) findViewById(R.id.image);
        head = (TextView) findViewById(R.id.heading);
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // compass
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // compass
        sensor.unregisterListener(this);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float d = Math.round(event.values[0]);
        head.setText("Heading: " + Float.toString(d) + " degrees");

        RotateAnimation ra = new RotateAnimation(
                degree,
                -d,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        ra.setDuration(210);
        ra.setFillAfter(true);

        img.startAnimation(ra);
        degree = -d + 90;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void scanBar(View v) {
        try {
            Intent in = new Intent(actionScan);
            in.putExtra("SCAN_MODE", "PRODUCT MODE");
            startActivityForResult(in, 0);
        }
        catch(ActivityNotFoundException e) {
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void scanQR(View v) {
        try {
            Intent in = new Intent(actionScan);
            in.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(in, 0);
        }
        catch(ActivityNotFoundException e) {
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final Activity a, CharSequence title, CharSequence message, CharSequence yes, CharSequence no) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(a);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent in = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    a.startActivity(in);
                }
                catch(ActivityNotFoundException e) {}
            }
        });
        dialog.setNegativeButton(no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int i) {}
        });
        return dialog.show();
    }

    public void onActivityResult(int request, int result, Intent in) {
        if(request == 0) {
            if(result == RESULT_OK) {
                String content = in.getStringExtra("SCAN_RESULT");
                String format = in.getStringExtra("SCAN_RESULT_FORMAT");
                Toast t = Toast.makeText(this, "Content:" + content + " Format:" + format, Toast.LENGTH_LONG);
                t.show();
            }
        }
    }
}
