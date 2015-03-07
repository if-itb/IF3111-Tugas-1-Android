package rasyad.carijerry;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    //konstanta qrcode
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    //sensor manager
    private SensorManager mSensorManager;

    //arah gambar kompas
    private float currentDegree = 0f;

    //peta kampus
    private final GMaps PetaKampus = new GMaps();

    private final Context me = this;
    TextView arah;

    private ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageViewCompass);

        getLokasiJerry();

        arah = (TextView) findViewById(R.id.arah);

        //inisialisasi sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //inisialisasi peta
        FragmentManager fragmentmanager = getFragmentManager();
        fragmentmanager.beginTransaction().replace(R.id.map_view, PetaKampus).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event){

        float degree = Math.round(event.values[0]);

        arah.setText("Arah: " + Float.toString(degree) + " derajat");

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        //lama animasi
        ra.setDuration(210);

        //set animasi after the end of the reservation status
        ra.setFillAfter(true);

        //mulai animasi
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    //qr code mode
    public void scanQR(View v) {
        try {
            //melakukan scanning dari zxing
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.download_decision);
            alertDialogBuilder.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {
                    }
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    //on activity result method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //mendapatkan kembalian dari internet
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content: " + contents + " Format: " + format, Toast.LENGTH_LONG);
                toast.show();

                kirimToken("13511045", contents);
            }
        }
    }

    public void kirimToken(final String nim, final String token){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://167.205.32.46/pbd/api/catch";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("debug res", response);
                        if (response.indexOf('{') == -1) {
                            Toast toast = Toast.makeText(me, "Jerry ditemukan!", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            long errCode;
                            String message;
                            Gson gson = new Gson();
                            response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
                            String json = gson.toJson(response);
                            try {
                                JSONObject jsonObj = new JSONObject(response);
                                errCode = jsonObj.getLong("code");
                                message = jsonObj.getString("message");

                                Toast toast = Toast.makeText(me, "Error with code " + errCode + " " + message, Toast.LENGTH_LONG);
                                toast.show();
                            }
                            catch (JSONException e){
                                Log.d("debug res", "exception json");
                            }
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

        )
            {
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

    public void  getLokasiJerry(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13511045";

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

                            PetaKampus.setJerry(lat, lng);

                            Date now = new Date();
                            long rightnow = now.getTime();

                            new CountDownTimer(time*1000 - rightnow, 1000) {

                                public void onTick(long millisUntilFinished) {

                                    ((TextView)findViewById(R.id.sisa_waktu)).setText("sisa waktu : " + millisUntilFinished/1000);
                                }

                                public void onFinish() {

                                    getLokasiJerry();
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
                Log.d("debug res", "Tidak berhasil!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}
