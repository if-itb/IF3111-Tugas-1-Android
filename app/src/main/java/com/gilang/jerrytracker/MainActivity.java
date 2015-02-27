package com.gilang.jerrytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gilang.fragment.ScannerFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback{

    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private double latitude = 0;
    private double longitude = 0;
    private SupportMapFragment mapFragment;
    private GoogleMap maps;
    private ImageView compassArrow;
    private Compass compass;
    private TextView scannerButton;
    private Toolbar toolbar;
    private ProgressBar proggresBar;
    private TextView retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        compassArrow = (ImageView) findViewById(R.id.compass_arrow);
        compass = new Compass(this, compassArrow);

        scannerButton = (TextView) findViewById(R.id.button_scanner);
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR(v);
            }
        });

        proggresBar = (ProgressBar) findViewById(R.id.action_bar_proggress);

        retryButton = (TextView)findViewById(R.id.retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.GONE);
                proggresBar.setVisibility(View.VISIBLE);
                getMapData();
            }
        });

        mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        maps = map;
        maps.setMyLocationEnabled(true);
        maps.moveCamera(CameraUpdateFactory.zoomTo(15));
        maps.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-6.893271, 107.610266)));
        getMapData();
    }

    public void getMapData(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://167.205.32.46/pbd/api/track?nim=13512045";
        // Request a string response from the provided URL.
        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            proggresBar.setVisibility(View.GONE);
                            JSONObject json = new JSONObject(response);
                            latitude = Double.valueOf(json.getString("lat"));
                            longitude = Double.valueOf(json.getString("long"));
                            maps.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title("Jerry's Location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry)));
                            if(maps.getMyLocation() != null){
                                maps.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                                        maps.getMyLocation().getLatitude(),
                                        maps.getMyLocation().getLongitude()
                                )));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            retryButton.setVisibility(View.VISIBLE);
                            proggresBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(), "An error occured\nTap retry to continue", Toast.LENGTH_LONG).show();
                        }
                }
        );
        req.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(3), 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        proggresBar.setVisibility(View.VISIBLE);
        queue.add(req);
    }

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
            if (resultCode == Activity.RESULT_OK) {

                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "http://167.205.32.46/pbd/api/catch";

                final JSONObject obj = new JSONObject();
                try {
                    obj.put("nim", "13512045");
                    obj.put("token", contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringRequest req = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                proggresBar.setVisibility(View.GONE);
                                retryButton.setVisibility(View.GONE);
                                Toast.makeText(getBaseContext(), "Success!\nReceived data : " +
                                        response.toString(), Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getBaseContext(), "Error : " + error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                ){

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        return obj.toString().getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }
                };
                req.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(3), 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                proggresBar.setVisibility(View.VISIBLE);
                queue.add(req);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compass.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        compass.start();
    }
}
