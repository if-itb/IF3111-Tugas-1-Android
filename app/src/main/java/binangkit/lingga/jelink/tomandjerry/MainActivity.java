package binangkit.lingga.jelink.tomandjerry;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jelink on 3/4/2015.
 */
public class MainActivity extends ActionBarActivity implements SensorEventListener{
    // define the display assembly compass picture
    private ImageView image;
    // QR constant
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    // Map ITB
    private final MapsFragment MapITB = new MapsFragment();

    private final Context me = this;

    TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        setContentView(R.layout.activity_main);

        // Get lokasi dari spike
        getLocationFromSpike();

        // Image untuk kompas
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //inisialisasi map
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.map_view, MapITB).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

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
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
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
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                //Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                //toast.show();

                sendTokenToSpike("13512059", contents);
            }
        }
    }

    public void sendTokenToSpike(final String nim, final String token){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://167.205.32.46/pbd/api/catch";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.d("debug res", response);
                    long code;
                    String message;
                    Gson gson = new Gson();
                    response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
                    String json = gson.toJson(response);
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        code = jsonObj.getLong("code");
                        message = jsonObj.getString("message");
                        if(code == 200) {
                            Toast toast = Toast.makeText(me, "Congrats! You found Jerry!!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else{
                                Toast toast = Toast.makeText(me, "Error with code " + code + " " + message, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    catch (JSONException e){
                        Log.d("debug res", "exception json");
                    }
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                    Log.d("debug res", error.getMessage());
                    }
            }
        ) {
        @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nim", nim);
                params.put("token", token);
                return params;
            }
        };
        queue.add(postRequest);

    }

    public void getLocationFromSpike(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13512059";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        double lat=0;
                        double lng=0;
                        long time=0;

                        // hapus formatting utf
                        Gson gson = new Gson();
                        response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
                        String json = gson.toJson(response);
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            lat = jsonObj.getDouble("lat");
                            lng = jsonObj.getDouble("long");
                            time = jsonObj.getLong("valid_until");

                            MapITB.setJerry(lat, lng);

                            Date now = new Date();
                            long sekarang = now.getTime();

                            new CountDownTimer(time*1000 - sekarang, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                                    ((TextView)findViewById(R.id.sisa_waktu)).setText("sisa waktu : " + millisUntilFinished/1000);
                                }

                                public void onFinish() {
                                    //mTextField.setText("done!");
                                    //MapITB.setJerry(-6.890754, 107.609435);
                                    getLocationFromSpike();
                                }
                            }.start();
                        }
                        catch (JSONException e){
                            Log.d("debug res", "exception json");
                        }
                        // tulis ke log
                        Log.d("debug res", "Response is: " + response + " " + lat + " " + lng + " " + time);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("debug res", "That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
