package com.yanfa.jerrysearcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String jsonRet;
    private Data data;
    private boolean loading;
    private FragmentManager fm;
    private SensorManager sm;

    public static ProgressBar loadBar;
    private Button QRButton;
    private Button jerryFinderButton;
    private String token;
    private float currentDegree = 0f;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loading = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadBar = (ProgressBar)findViewById(R.id.mapLoad);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        fm = getSupportFragmentManager();

        fm.beginTransaction().replace(R.id.container, new MapFragment())
                .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();

        QRButton = (Button) findViewById(R.id.QRButton);
        QRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR(v);
            }
        });
//        QRButton.setOnTouchListener(new View.OnTouchListener() {
//            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                QRButton.setScaleX(0.9f);
//                QRButton.setScaleY(0.9f);
//                return false;
//            }
//
//
//        });

        jerryFinderButton = (Button)findViewById(R.id.jerryButton);
        jerryFinderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CompassDialogFragment()
                        .show(fm, "MyDialog");
            }


        });
//        jerryFinderButton.setOnTouchListener(new View.OnTouchListener() {
//            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                jerryFinderButton.setScaleX(0.9f);
//                jerryFinderButton.setScaleY(0.9f);
//                return false;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
    }

    //product barcode mode
    //product barcode mode
    public void scanBar(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog((Activity)this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
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
            //showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
            Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (Exception e) {

            }
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
    public void onActivityResult(final int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent

                loadBar.setVisibility(View.VISIBLE);
                loadBar.bringToFront();

                final String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, "Content: " + contents, Toast.LENGTH_LONG);
                toast.show();

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "http://167.205.32.46/pbd/api/catch";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Result Retrieved");
                                alertDialog.setMessage(response.substring(3,response.length()));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                loadBar.setVisibility(View.GONE);
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getBaseContext(), "Error" + error, Toast.LENGTH_LONG).show();
                                loadBar.setVisibility(View.GONE);
                            }

                        }
                ) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Content-type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("token", contents);
                        params.put("nim","13512037");
                        return params;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String temp = "{\"nim\": \"13512037\",\"token\": \"" + contents + "\"}";
                        return temp.getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }
                };


                postRequest.setRetryPolicy(new DefaultRetryPolicy(
                        1000*5,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(postRequest);

            }
        }
    }
}
