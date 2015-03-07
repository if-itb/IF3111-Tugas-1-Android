package com.sisters.khorosho;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Globals.sBypass) {
            Globals.sBypass = false;
            try {
                new DownloadJerryInfoTask().execute(Globals.TRACK_ENDPOINT).get(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            Toast.makeText(this,"Jerry got away!!",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ShowMap.class);
            startActivity(intent);
        }
    }

    public void askSpike(View view) {
        //TODO shit here, ask for the location and start next activity
        String stringUrl = Globals.TRACK_ENDPOINT;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(this,"Retrieving Jerry's Location, Standby!",Toast.LENGTH_SHORT).show();
            DownloadJerryInfoTask dl = new DownloadJerryInfoTask();
            try {
                dl.execute(stringUrl).get(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this,"Timeout in retrieving, retry!",Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this,"Location get!!",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ShowMap.class);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Fail, Please check network connection!",Toast.LENGTH_LONG).show();
        }
    }
}

class DownloadJerryInfoTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(params[0]);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                //Log.e(ParseJSON.class.toString(), "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        JSONObject json;

        try {
            json = new JSONObject(result);
            Globals.sJerryLat = json.getDouble("lat");
            Globals.sJerryLong = json.getDouble("long");
            Globals.sValidUntil = json.getLong("valid_until");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

