package com.tracker.timothypratama.tomandjerryapplication.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tracker.timothypratama.tomandjerryapplication.Model.TrackJerryViewModel;
import com.tracker.timothypratama.tomandjerryapplication.R;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class TrackJerry extends ActionBarActivity {

    Timer timer;
    final int update_rate = 3000; /* in milisecond */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_jerry);
        timer = new Timer();
        StartUpdater();
    }

    private void StartUpdater() {
        final Handler handler = new Handler();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new Updater().execute("http://167.205.32.46/pbd/api/track?nim=13512032");
                    }
                });
            }
        };
        Log.d("Timer", "Timer Started!");
        timer.schedule(timerTask, 0, update_rate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_jerry, menu);

        return true;
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

    public void ViewMap(View view) {
        Intent i = new Intent(this,com.tracker.timothypratama.tomandjerryapplication.Activity.GPSTracking.class);
        startActivity(i);
    }

    class Updater extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            Log.d("Async Task", "Async Task Updater Created!");
            String response = "";
            for(String url: params) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execeute = client.execute(httpGet);
                    InputStream content = execeute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine())!= null) {
                        response += s;
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {

            JSONObject jsonObject;
            double Latitude;
            double Longitude;
            String valid;

            try {
                jsonObject = new JSONObject(s);
                Latitude = jsonObject.getDouble("lat");
                Longitude = jsonObject.getDouble("long");
                valid = jsonObject.getString("valid_until");

                TrackJerryViewModel.setLongitude(Longitude);
                TrackJerryViewModel.setLatitude(Latitude);

                TextView lat = (TextView) findViewById(R.id.LatValueTextView);
                TextView longg = (TextView) findViewById(R.id.LongValueTextView);
                TextView val = (TextView) findViewById(R.id.ValidUntilValueTextView);
                lat.setText(String.valueOf(Latitude));
                longg.setText(String.valueOf(Longitude));
                val.setText(valid);

                Log.d("Response", s);
                Log.d("Latitude", String.valueOf(Latitude));
                Log.d("Longitude", String.valueOf(Longitude));
                Log.d("Valid Until", valid);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
