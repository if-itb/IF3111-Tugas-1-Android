package binangkit.lingga.jelink.tomandjerry;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jelink on 3/4/2015.
 * QR Scanner reference : http://examples.javacodegeeks.com/android/android-barcode-and-qr-scanner-example/
 * Compass reference : http://www.javacodegeeks.com/2013/09/android-compass-code-example.html
 */
public class MainActivity extends ActionBarActivity implements SensorEventListener{
    /**
     *  ATRIBUT-ATRIBUT
     *  */

    /** KOMPAS */
    /** Gambar kompas */
    private ImageView compassImage;
    /** QR constant */
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    /** derajat kompas */
    private float currentDegree = 0f;
    /** Tampilan pada layout untuk derajat */
    TextView degHeading;
    /** device sensor manager */
    private SensorManager mSensorManager;

    /** PETA ITB */
    /** Fragment untuk peta ITB */
    private final MapsFragment MapITB = new MapsFragment();
    /** Fragment manager */
    FragmentManager fragmentManager;

    /** METHOD-METHOD */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set layout
        setContentView(R.layout.activity_main);
        // Ambil lokasi dari server spike
        getLocationFromSpike();
        // Instansiasi image untuk kompas
        compassImage = (ImageView) findViewById(R.id.imageViewCompass);
        // Menginisialisasi degHeading dengan elemen layout yang akan menuliskan derajat
        degHeading = (TextView) findViewById(R.id.tvHeading);
        // Inisailisasi sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Inisialisasi peta ITB dari fragment ke layout
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.map_view, MapITB).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // meregister listener
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // menghentikan listener untuk menghemat baterai
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // cari sudut rotasi sumbu z
        float degree = Math.round(event.values[0]);
        // menuliskan ke elemen layout degHeading
        degHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        // animasi rotasi
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // durasi animasi
        ra.setDuration(210);
        ra.setFillAfter(true);
        // Start animasi
        compassImage.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    /** Scan QR Code untuk mendapatkan token */
    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException exc) {
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
                    // do nothing
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    /** Bila scan QR menemukan token, mengirimnya ke Spike */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                sendTokenToSpike("13512059", contents);
            }
        }
    }

    /** Mengirimkan token dan nim ke server spike */
    public void sendTokenToSpike(final String nim, final String token) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final int status_code;
        final String url = "http://167.205.32.46/pbd/api/catch";
        final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
            // bila ada response (network code 200)
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    long code;
                    String message;
                    response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        code = jsonObj.getLong("code");
                        message = jsonObj.getString("message");
                        if(code == 200) {
                            displayMessage("Congrats! You found Jerry!!");
                        }
                        else{
                            displayMessage("Error with code " + code + " " + message);
                        }
                    }
                    catch (JSONException e) {
                        displayMessage("JSON parsing error");
                    }
                }
            },
            // bila tidak ada response (network code bukan 200)
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                    displayMessage("Token salah, jangan curang yaa");
                }
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nim", nim);
                params.put("token", token);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(postRequest);
    }

    /** Menampilkan message pada toast, pada context ini */
    public void displayMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /** Merequest lokasi Jerry kepada Spike menggunakan method Get **/
    public void getLocationFromSpike() {
        // Mengeset request code
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13512059";
        // Melakukan request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        double lat=0;
                        double lng=0;
                        long time=0;
                        // hapus formatting utf
                        response = response.substring(response.indexOf('{'),response.lastIndexOf('}')+1);
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
                                    ((TextView)findViewById(R.id.sisa_waktu)).setText("sisa waktu : " + millisUntilFinished/1000);
                                }

                                public void onFinish() {
                                    getLocationFromSpike();
                                }
                            }.start();
                        }
                        catch (JSONException e) {
                            displayMessage("JSON parsing error");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMessage("Mohon tunggu, tidak dapat menjangkau server.");
                new CountDownTimer(5000, 1000) {
                    public void onFinish() {
                        getLocationFromSpike();
                    }
                    public void onTick(long ms) {
                        //do notinng
                    }
                }.start();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
