package com.android.map.jerryfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private ScheduledExecutorService scheduleTaskExecutor;
    JSONObject json = null;
    long epochTime;
    int nanana = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView lat = (TextView) findViewById(R.id.textViewLatitude);
        final TextView lon = (TextView) findViewById(R.id.textViewLongitude);
        final TextView dat = (TextView) findViewById(R.id.textViewExpiryDate);
        final TextView ref = (TextView) findViewById(R.id.textViewRefresh);
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                nanana = nanana + 1;

                try {
                    json = new GetJSON().execute("http://167.205.32.46/pbd/api/track?nim=13512055.json").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // UI update
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if(json != null) {
                                lat.setText("Latitude: " + json.getDouble("lat"));
                                lon.setText("Longitude: " + json.getDouble("long"));

                                epochTime = json.getLong("valid_until");

                                dat.setText("Valid until: " + convertEpochToDate(epochTime));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ref.setText("refreshed" + nanana);
                    }
                });
                }
            }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void mapButtonOnClick(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private String convertEpochToDate(long epochTime){
        String formattedDate = null;

        Date date = new Date(epochTime * 1000L);
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));
        formattedDate = format.format(date);

        return formattedDate;
    }

    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
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

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                String content = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                int statusCode = 0;
                String message = "Jerry missing";

                JSONObject json = new JSONObject();

                try {
                    json.put("nim", "13512055");
                    json.put("token", content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    statusCode = new PostJSON().execute(json).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if(statusCode == 200){
                    message = "Jerry successfully catched";
                }
                if(statusCode == 400){
                    message = "Missing parameter";
                }
                if(statusCode == 403){
                    message = "Forbidden";
                }

                Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }



}
