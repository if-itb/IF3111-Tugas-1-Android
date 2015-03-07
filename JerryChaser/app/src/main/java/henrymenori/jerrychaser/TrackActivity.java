package henrymenori.jerrychaser;

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
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class TrackActivity extends FragmentActivity implements SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // attribute for compass
    private ImageView img; // compass picture
    private float degree = 0f; // angle for compass picture
    private SensorManager sensor; // device sensor

    // attribute for scan QR code
    static final String scan = "com.google.zxing.client.android.SCAN"; // source for QR scanner

    // attribute for tracker
    private String content; // content for get and post
    private long time; // time remaining for jerry

    // attribute text
    TextView countdown; // show remaining time for jerry


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // compass initialization
        img = (ImageView) findViewById(R.id.image);
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        // tracker initialization
        countdown = (TextView) findViewById(R.id.text2);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // compass
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        // tracker
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // compass
        sensor.unregisterListener(this);
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
        getRequest();
    }

    // compass function
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degreeChange = Math.round(event.values[0]);

        RotateAnimation ra = new RotateAnimation(
                degree,
                -degreeChange,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        ra.setDuration(210);
        ra.setFillAfter(true);

        img.startAnimation(ra);
        degree = -degreeChange + 90;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // method not use
    }

    // scanner function
    public void scanQR(View v) {
        try {
            Intent in = new Intent(scan);
            in.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(in, 0);
        }
        catch(ActivityNotFoundException e) {
            showDialog(TrackActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                catch(ActivityNotFoundException e) {
                    e.printStackTrace();
                }
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
                content = in.getStringExtra("SCAN_RESULT");
                String format = in.getStringExtra("SCAN_RESULT_FORMAT");
                Toast t = Toast.makeText(this, "Content:" + content + " Format:" + format, Toast.LENGTH_LONG);
                t.show();
                postRequest();
            }
        }
    }

    // tracker function
    public void getRequest() {
        GetTask gt = new GetTask();
        gt.execute("http://167.205.32.46/pbd/api/track?nim=13512082");
    }

    private class GetTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(params[0]));

            JSONObject json = null;
            try {
                HttpResponse response = client.execute(request);

                HttpEntity entity = null;
                if(response != null) {
                    entity = response.getEntity();
                }

                BufferedHttpEntity bufEntity = new BufferedHttpEntity(entity);
                BufferedReader bufRead = new BufferedReader(new InputStreamReader(bufEntity.getContent()));

                String s;
                StringBuilder sb = new StringBuilder();
                while((s = bufRead.readLine()) != null) {
                    sb.append(s);
                }

                json = new JSONObject(sb.toString());
                content = json.toString();
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try{
                double lat = Double.parseDouble(result.getString("lat"));
                double lng = Double.parseDouble(result.getString("long"));
                time = Long.parseLong(result.getString("valid_until")) * 1000;

                LatLng jerry = new LatLng(lat,lng);
                new CountDownTimer((time - System.currentTimeMillis()), 1000) {
                    public void onTick(long remainder) {
                        long r = (time - System.currentTimeMillis()) / 1000;
                        countdown.setText(String.valueOf(
                                "Sisa waktu : "
                                        + (r / 3600) + " jam "
                                        + (r % 3600 / 60) + " menit "
                                        + (r % 3600 % 60) + " detik"));
                    }

                    @Override
                    public void onFinish() {
                        setUpMapIfNeeded();
                    }
                }.start();

                Toast toast = Toast.makeText(TrackActivity.this, "Jerry Position ("+lat+","+lng+")", Toast.LENGTH_LONG);
                toast.show();

                // show marker
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(jerry).title("Marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jerry, 17));
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void postRequest(){
        PostTask pt = new PostTask();
        pt.execute();
    }

    private class PostTask extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params){
            HttpClient client = new DefaultHttpClient();

            String result = null;
            try{
                HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");

                JSONObject json = new JSONObject();
                json.put("nim", "13512082");
                json.put("token", content);

                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

                post.setEntity(se);
                HttpResponse response = client.execute(post);
                if(response != null) {
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));

                    String line;
                    StringBuilder sb = new StringBuilder();
                    while((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    json = new JSONObject(sb.toString());
                    if(json.getInt("code") == 200) {
                        result = "Jerry berhasil ditangkap!";
                    }
                    else {
                        result = "Jerry berhasil kabur";
                    }
                }
            }
            catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Toast toast = Toast.makeText(TrackActivity.this, result, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
