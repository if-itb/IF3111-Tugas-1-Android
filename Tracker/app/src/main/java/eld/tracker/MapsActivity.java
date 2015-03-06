package eld.tracker;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements SensorEventListener{
    //Map attributes
    private double lat;
    private double lng;
    private LatLng tes = new LatLng(-3.890323,107.610381);
    private LatLng JerryPosition = tes;
    static final LatLngBounds ITB = new LatLngBounds(new LatLng(-6.891476, 107.608229),new LatLng(-6.891438, 107.612242));
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private String contents = "";

    //Compass attributes
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree + 90;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getRequest();

        // setting compass
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //setting maps
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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
        mMap.addMarker(new MarkerOptions().position(JerryPosition).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JerryPosition, 17));
    }


    // QRcode
    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MapsActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
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
                } catch (ActivityNotFoundException e) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
                postRequest();
            }
        }
    }

    //GetRequest
    public void getRequest(){
        GetTask GT = new GetTask();
        GT.execute("http://167.205.32.46/pbd/api/track?nim=13512002");
    }

    private class GetTask extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... uri) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(uri[0]));
            HttpResponse response = null;
            JSONObject json = null;
            HttpEntity entity = null;
            BufferedHttpEntity buffEntity = null;
            BufferedReader rd = null;
            StringBuffer sb = new StringBuffer();
            String line = "";
            try{
                response = (HttpResponse) client.execute(request);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            entity = response.getEntity();
            try{
                buffEntity = new BufferedHttpEntity(entity);
                rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                while((line = rd.readLine()) != null){
                    sb.append(line);
                }
                try{
                    json = new JSONObject(sb.toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            contents = json.toString();
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try{
                lat = Double.parseDouble(result.getString("lat"));
                lng = Double.parseDouble(result.getString("long"));
                JerryPosition = new LatLng(lat,lng);
                Toast toast = Toast.makeText(MapsActivity.this,"Jerry Position ("+lat+","+lng+")",Toast.LENGTH_LONG);
                toast.show();
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            setUpMap();
        }
    }

    //Post
    public void postRequest(){
        PostTask PT = new PostTask();
        PT.execute();
    }

    private class PostTask extends AsyncTask<Void, String, String>{
        @Override
        protected String doInBackground(Void... uri){
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;
            JSONObject json = new JSONObject();
            String content = "";
            HttpPost post = null;
            StringEntity se = null;
            HttpEntity entity = null;
            BufferedHttpEntity buffEntity = null;
            BufferedReader rd = null;
            StringBuffer sb = new StringBuffer();
            String line = "";
            try{
                post = new HttpPost("http://167.205.32.46/pbd/api/catch");
//                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//                pairs.add(new NameValuePair("nim","13512002"));
//                pairs.add(new NameValuePair("token",contents));
                json.put("nim", "13512002");
                json.put("token", contents);
                se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
//                post.setEntity(new UrlEncodedFormEntity(pairs));
                response = client.execute(post);
                if(response != null){
                    entity = response.getEntity();
                    buffEntity = new BufferedHttpEntity(entity);
                    rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));
                    while((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    content = sb.toString();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Toast toastreply = Toast.makeText(MapsActivity.this,result,Toast.LENGTH_LONG);
            setUpMapIfNeeded();
            toastreply.show();
        }
    }
}
