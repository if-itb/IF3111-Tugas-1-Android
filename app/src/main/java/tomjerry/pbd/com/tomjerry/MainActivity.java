package tomjerry.pbd.com.tomjerry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private long valid_until;
    private long curr_time;
    private String statusResult = "";
    private boolean lock;
    private TextView timer;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mengambil parameter dari intent SplashActivity
        Bundle extras = getIntent().getExtras();
        latitude = extras.getDouble("latitude");
        longitude = extras.getDouble("longitude");
        valid_until = extras.getLong("valid_until");

        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        Date date = new Date();
        curr_time = date.getTime();
        valid_until = valid_until * 1000;
        long duration = valid_until - curr_time;
        if(duration<=0) {
            duration = 30000;
        }

        // Membuat timer yang akan mengrefresh apabila mencapai 0
        timer = (TextView) findViewById(R.id.timer);
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalsec = millisUntilFinished / 1000;

                long day = totalsec / (3600 * 24);
                totalsec = totalsec - (day * 3600 * 24);

                long hrs = totalsec / 3600;
                totalsec = totalsec - (hrs * 3600);

                long mnt = totalsec / 60;
                totalsec = totalsec - (mnt * 60);

                timer.setText(day+"d "+hrs+"h "+mnt+"m "+totalsec+"s");
            }

            public void onFinish() {
                Intent intent = new Intent(MainActivity.this,SplashActivity.class);
                countDownTimer.cancel();
                finish();
                startActivity(intent);
            }
        };
        countDownTimer.start();

        // Tombol untuk mengstart Barcode Scanner
        Button captureButton = (Button) findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                countDownTimer.cancel();
                startActivityForResult(intent, 0);
            }
        });

        // Tombol untuk mengstart Compasss
        Button compassButton = (Button) findViewById(R.id.compass_button);
        compassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CompassActivity.class);
                countDownTimer.cancel();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        backButtonHandler();
    }

    // Mengoverride tombol back untuk mengecek apakah user memang ingin keluar dari aplikasi
    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Leave application?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to leave the application?");
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        countDownTimer.cancel();
                        finish();
                    }
                });
        // Mengeluarkan Alert Message
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String scanResult = data.getStringExtra("SCAN_RESULT");
                lock = true;

                // Thread untuk mengpost hasil dari Barcode Scanning
                Thread postThread = new Thread() {
                    @Override
                    public void run() {
                    try {
                        super.run();
                        try {
                            HttpClient client = new DefaultHttpClient();
                            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                            post.setHeader("Content-type","application/json");

                            JSONObject jsonObj = new JSONObject();
                            StringEntity entity;
                            jsonObj.put("nim", "13512025");
                            jsonObj.put("token", scanResult);
                            entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);

                            entity.setContentType("application/json");
                            post.setEntity(entity);

                            HttpResponse response = client.execute(post);

                            // Mengambil isi dari response Http Get
                            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                            String line;
                            while ((line = rd.readLine()) != null) {
                                statusResult += line;
                            }
                            JSONObject object = new JSONObject(statusResult);
                            statusResult = object.getString("message");
                            lock = false;
                        } catch (Exception e) {
                            statusResult = "Failed to post";
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        statusResult = "Failed to post";
                    }
                    }
                };
                postThread.start();
                while(lock) {}
                Toast.makeText(getApplicationContext(), statusResult, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast.makeText(getApplicationContext(), "Capturing cancelled",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Date date = new Date();
        curr_time = date.getTime();
        long duration = valid_until - curr_time;
        if(duration<=0) {
            duration = 30000;
        }

        // Mengresume countdown
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalsec = millisUntilFinished / 1000;

                long day = totalsec / (3600 * 24);
                totalsec = totalsec - (day * 3600 * 24);

                long hrs = totalsec / 3600;
                totalsec = totalsec - (hrs * 3600);

                long mnt = totalsec / 60;
                totalsec = totalsec - (mnt * 60);

                timer.setText(day+"d "+hrs+"h "+mnt+"m "+totalsec+"s");
            }

            public void onFinish() {
                Intent intent = new Intent(MainActivity.this,SplashActivity.class);
                countDownTimer.cancel();
                finish();
                startActivity(intent);
            }
        };
        countDownTimer.start();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            // Membuat marker untuk menandakan lokasi Jerry
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Jerry is here!"));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)).zoom(14).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
