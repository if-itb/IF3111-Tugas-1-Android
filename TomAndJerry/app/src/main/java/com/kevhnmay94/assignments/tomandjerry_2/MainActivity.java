package com.kevhnmay94.assignments.tomandjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity {
    private final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private final String nim = "13512044";
    private long epoch;
    private double latval;
    private double lonval;


    public void onSpikeButtonClick(View view) {
        String geturl = "http://167.205.32.46/pbd/api/track?nim=" + nim;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, geturl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            latval = response.getDouble("lat");
                            lonval = response.getDouble("long");
                            epoch = response.getLong("valid_until") * 1000;
                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(MainActivity.this, "Jerry not found", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        showFindDialog(MainActivity.this).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    public void onSearchButtonClick(View view) {
        Intent mapIntent = new Intent(this, FindJerryActivity.class);
        mapIntent.putExtra("latitude",latval);
        mapIntent.putExtra("longitude",lonval);
        startActivity(mapIntent);
    }

    public void onCatchButtonClick(View view) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showErrorDialog(this).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private AlertDialog showErrorDialog(final Activity act) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle("Error");
        downloadDialog.setMessage("No QR code scanner");

        downloadDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return downloadDialog.show();
    }

    private AlertDialog showFindDialog(final Activity act)  {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        String formatted = format.format(epoch);
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle("Spike says:");
        downloadDialog.setMessage("Latitude: " + Double.toString(latval) + "\n" + "Longitude: " + Double.toString(lonval) +
                "\n" + "Valid Until: " + formatted + "\n" + "Find Jerry now?");
        downloadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mapIntent = new Intent(MainActivity.this, FindJerryActivity.class);
                mapIntent.putExtra("latitude", latval);
                mapIntent.putExtra("longitude", lonval);
                act.startActivity(mapIntent);
            }
        });

        downloadDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = intent.getStringExtra("SCAN_RESULT");
                HashMap<String,String> params = new HashMap<>();
                params.put("nim",nim);
                params.put("token",contents);
                JSONObject postreq = new JSONObject(params);
                String posturl = "http://167.205.32.46/pbd/api/catch";
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, posturl, postreq, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int retval = response.getInt("code");
                                    if(retval == 200)
                                        Toast.makeText(MainActivity.this, "Well done! You catch Jerry!", Toast.LENGTH_LONG).show();
                                    else if(retval == 403)
                                        Toast.makeText(MainActivity.this, "Jerry is in another castle!", Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(MainActivity.this, "Instruction unclear, Jerry not found", Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    Toast toast = Toast.makeText(MainActivity.this, "Spike doesn't respond", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                };
                RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
            }
        }
    }

}
