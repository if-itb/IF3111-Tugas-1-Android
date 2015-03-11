package id.web.tkesgar.if3111catandmouse;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class TrackerActivity extends Activity implements LocationListener {

    private static final String TAG = "Tracker";

    private static final int ACTIVITY_SCAN = 101;

    private LocationManager locationManager;

    private HttpClient http;

    private double currentLatitude;

    private double currentLongitude;

    private double targetLatitude;

    private double targetLongitude;

    private String code;

    private TextView currentLatitudeText;

    private TextView currentLongitudeText;

    private TextView targetLatitudeText;

    private TextView targetLongitudeText;

    private TextView codeText;

    private Button catchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tracker);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert (locationManager != null);

        http = new DefaultHttpClient();
        assert (http != null);

        currentLatitudeText = (TextView) findViewById(R.id.text_current_latitude);
        assert (currentLatitudeText != null);

        currentLongitudeText = (TextView) findViewById(R.id.text_current_longitude);
        assert (currentLongitudeText != null);

        targetLatitudeText = (TextView) findViewById(R.id.text_target_latitude);
        assert (targetLatitudeText != null);

        targetLongitudeText = (TextView) findViewById(R.id.text_target_longitude);
        assert (targetLongitudeText != null);

        codeText = (TextView) findViewById(R.id.text_code);
        assert (codeText != null);

        catchButton = (Button) findViewById(R.id.button_catch);
        assert (catchButton != null);

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            setCurrentLatitude(lastLocation.getLatitude());
            setCurrentLatitude(lastLocation.getLongitude());
        }

        setCode(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10 * 1000,
                0,
                this
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        setCurrentLatitude(location.getLatitude());
        setCurrentLongitude(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String message = null;
        switch (status) {
            case LocationProvider.AVAILABLE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i(TAG, String.format("%s is temporarily unavailable", provider));
                message = "Location service is temporarily unavailable.";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.i(TAG, String.format("%s is out of service", provider));
                message = "Location service is out of service.";
                break;
            default:
                Log.i(TAG, String.format("%s is %d", provider, status));
                break;
        }
        if (message != null) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, String.format("%s is enabled", provider));

        String message = "Location service is enabled.";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, String.format("%s is disabled", provider));

        Toast.makeText(
                getApplicationContext(),
                "Please enable location service in order for this app to properly work.",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SCAN) {
            if (resultCode == RESULT_OK) {
                String scanResult = data.getStringExtra("SCAN_RESULT");
                setCode(scanResult);
            }
        }
    }

    private void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
        currentLatitudeText.setText(String.format("%.6f", currentLatitude));
    }

    private void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
        currentLongitudeText.setText(String.format("%.6f", currentLongitude));
    }

    private void setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
        targetLatitudeText.setText(String.format("%.6f", targetLatitude));
    }

    private void setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
        targetLongitudeText.setText(String.format("%.6f", targetLongitude));
    }

    private void setCode(String code) {
        this.code = code;
        if (code == null || code.isEmpty()) {
            codeText.setText("");
            catchButton.setEnabled(false);
        } else {
            codeText.setText(code);
            catchButton.setEnabled(true);
        }
    }

    private JSONObject doHttpTrack() throws JSONException, IOException {

        HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13511018");
        HttpResponse response = http.execute(request);
        HttpEntity entity = response.getEntity();

        InputStream stream = entity.getContent();
        Scanner scanner = new Scanner(new BufferedInputStream(stream)).useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        return new JSONObject(content);
    }

    private int doHttpCatch() throws IOException {

        JSONObject requestObject = new JSONObject();
        try {
            requestObject
                .put("token", code)
                .put("nim", Integer.toString(13511018));
        } catch (JSONException e) {
            Log.wtf(TAG, "bad JSON operation", e);
        }

        HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch");
        request.setHeader("Content-Type", "application/json");
        try {
            request.setEntity(new StringEntity(requestObject.toString()));
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, "bad encoding", e);
        }

        HttpResponse response = http.execute(request);
        return response.getStatusLine().getStatusCode();
    }

    public void onTrack(View buttonTrack) {
        Log.i(TAG, "clicked track");

        new AsyncTask<Void, Void, Void>() {

            private void showToast(final String message) {
                TrackerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final JSONObject track = doHttpTrack();
                    TrackerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                setTargetLatitude(track.getDouble("lat"));
                                setTargetLongitude(track.getDouble("long"));
                            } catch (JSONException e) {
                                Log.wtf(TAG, "bad JSON received from server", e);
                                showToast("Bad JSON received. Please contact course assistant for information.");
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "failed to receive data from server", e);
                    showToast("Failed to receive data from server. Please try again later.");
                } catch (JSONException e) {
                    Log.wtf(TAG, "bad JSON received", e);
                    showToast("Bad JSON received. Please contact course assistant for information.");
                }
                return null;
            }

        }.execute();
    }

    public void onScan(View buttonScan) {
        Log.i(TAG, "clicked scan");

        // http://stackoverflow.com/questions/8830647/how-to-scan-qrcode-in-android
        try {
            startActivityForResult(
                    new Intent("com.google.zxing.client.android.SCAN")
                        .putExtra("SCAN_MODE", "QR_CODE_MODE"),
                    ACTIVITY_SCAN
            );
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.zxing.client.android")));
        }
    }

    public void onCatch(View buttonCatch) {
        Log.i(TAG, "clicked catch");

        new AsyncTask<Void, Void, Void>() {

            private void showToast(final String message) {
                TrackerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final int responseCode = doHttpCatch();
                    TrackerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (responseCode) {
                                case 200:
                                    showToast("Successfully caught that certain mouse.");
                                    break;
                                case 400:
                                    Log.wtf(TAG, "received 400 missing parameter");
                                    showToast("Missing request parameters. Please stomp me.");
                                    break;
                                case 403:
                                    showToast("Wrong code sent. Please rescan code and try again.");
                                    break;
                                default:
                                    Log.wtf(TAG, String.format("bad code %d received", responseCode));
                                    showToast(String.format("Received bad code %d. Please contact course assistant for information.", responseCode));
                                    break;
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "failed to receive data from server", e);
                    showToast("Failed to receive data from server. Please try again later.");
                }
                return null;
            }

        }.execute();
    }
}
