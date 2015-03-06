package com.findjerry;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RefreshTrackingJerryActivity extends ActionBarActivity {
    private Double latitude;
    private Double longitude;
    private String valid_until;
    private long valid_until_long;
    private HandleJSON obj;
    private boolean lock;
    private String url1 = "http://167.205.32.46/pbd/api/track?nim=13512006";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_tracking_jerry);

        lock = true;

        new Task().execute(getApplicationContext());

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    while(lock) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent(RefreshTrackingJerryActivity.this,
                            AskSpikeActivity.class);
                    i.putExtra("latitude",latitude);
                    i.putExtra("longitude",longitude);
                    i.putExtra("valid_until",valid_until);
                    i.putExtra("valid_until_long",valid_until_long);
                    startActivity(i);
                    finish();
                }
            }
        };
        thread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh_tracking_jerry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public class Task extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... params) {
            Context context = params[0];
            System.out.println(context.toString());

            obj = new HandleJSON(url1);
            obj.fetchJSON();

            while(obj.parsingComplete);
            String lat = obj.getLat();
            latitude = Double.parseDouble(lat);
            String lon = obj.getLon();
            longitude = Double.parseDouble(lon);
            valid_until = obj.getValid_until();
            valid_until_long = Long.parseLong(obj.getValid_until());
            long epoch = Long.parseLong(valid_until);
            Date valid = new Date(epoch*1000);
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            valid_until = formatter.format(valid);

            lock = false;
            return null;
        }
    }
}
