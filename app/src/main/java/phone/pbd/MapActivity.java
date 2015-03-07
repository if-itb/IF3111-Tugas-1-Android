package phone.pbd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class MapActivity extends FragmentActivity implements SensorEventListener {
    private Position position;
    private GoogleMap googleMap;
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private TextView timeRemainingTextView = null;
    private CountDownTimer countDownTimer;
    private MarkerOptions markerOptions;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        image = (ImageView) findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        timeRemainingTextView = (TextView) findViewById(R.id.timeRemaining);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) { //untuk hemat batere
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.displayMap)).getMap();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
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

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        final boolean[] lock = {true};
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                final String[] res = {""};
                Thread postThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            super.run();
                            HttpClient client = new DefaultHttpClient();
                            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                            post.setHeader("Content-type","application/json");

                            JSONObject jsonObj = new JSONObject();
                            StringEntity entity;
                            jsonObj.put("nim", "13512065");
                            jsonObj.put("token", contents);
                            entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);

                            entity.setContentType("application/json");
                            post.setEntity(entity);

                            HttpResponse response = client.execute(post);

                            // Mengambil isi dari response Http Get
                            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                            String line;
                            while ((line = rd.readLine()) != null) {
                                res[0] += line;
                            }
                            JSONObject object = new JSONObject(res[0]);
                            res[0] = object.getString("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            res[0] = "Failed to post";
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                            res[0] = "Failed to post";
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            res[0] = "Failed to post";
                        } catch (IOException e) {
                            e.printStackTrace();
                            res[0] = "Failed to post";
                        } finally {
                            lock[0] = false;
                        }
                    }
                };
                postThread.start();
                while(lock[0]) {}
                if (res[0].equalsIgnoreCase("200")) {
                    Toast.makeText(this, "Jerry berhasil ditangkap!! Selamat!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Jerry gagal ditangkap. :(\nStatus: " +res[0] , Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        if (position == null || !position.isStillValid()) validateConnection();
    }

    private void validateConnection() {
        if (Helper.isOnline(getApplicationContext())) {
            new HttpActivity().execute(getApplicationContext());
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        } else {
            mSensorManager.unregisterListener(this); //hemat batere
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            moveTaskToBack(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            validateConnection();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setMessage("Lo ga punya internet? Mau keluar ga?").setPositiveButton("Ya! Saya keluar!", dialogClickListener)
                    .setNegativeButton("Ga! Coba lagi!", dialogClickListener).setCancelable(false).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); //hemat batere
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) countDownTimer.cancel(); //hemat batere
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (countDownTimer != null) countDownTimer.cancel();
        if (position.isStillValid()) {
            countDownTimer = new CountDownTimer(position.getValUntilRemainingInMilliseconds(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeRemainingTextView.setText("Time Remaining =  " + millisUntilFinished/1000 + " s");
                }

                @Override
                public void onFinish() {
                    setUpMapIfNeeded();
                    new HttpActivity().execute(getApplicationContext());
                }
            };
        }
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel(); //hemat batere
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
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
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MapActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public class HttpActivity extends AsyncTask<Context, Void, Void> {
        private String res = "";
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MapActivity.this, "Loading..", "Tanya Spike posisi Jerry", true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.setTitle("Success..");
            dialog.setMessage("Posisi Jerry berhasil didapatkan!");
            dialog.dismiss();

            //Get locationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            //set map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            //enable the location
            googleMap.setMyLocationEnabled(true);

            double latitude = position.getLatitude();
            double longitude = position.getLongitude();

//            latitude = Math.random() * 89;
//            longitude = Math.random() * 100;
            //Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            //Show the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            //Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            if (markerOptions == null) {
                markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude, longitude)).title("Jerry is here!");
                googleMap.addMarker(markerOptions);
            } else {
                markerOptions.position(new LatLng(latitude, longitude)).title("Jerry is here!");
            }
//            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
            if (countDownTimer != null) countDownTimer.cancel();
            if (position.isStillValid()) {
                countDownTimer = new CountDownTimer(position.getValUntilRemainingInMilliseconds(), 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeRemainingTextView.setText("Time Remaining =  " + millisUntilFinished/1000 + " s");
                    }

                    @Override
                    public void onFinish() {
                        setUpMapIfNeeded();
                        new HttpActivity().execute(getApplicationContext());
                    }
                };
            }
            countDownTimer.start();
        }

        @Override
        protected Void doInBackground(Context... params) {
            HttpGet httpGet = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512065");
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse;
            try {
                httpResponse = client.execute(httpGet);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String line = "";

                while ((line = br.readLine()) != null) {
                    res += line;
                }

                if (!res.equalsIgnoreCase("")) {
                    JSONObject object = new JSONObject(res);
                    double lat = object.getDouble("lat");
                    double longitude = object.getDouble("long");
                    long valUntil = object.getLong("valid_until");
                    position = new Position(lat, longitude, valUntil);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
