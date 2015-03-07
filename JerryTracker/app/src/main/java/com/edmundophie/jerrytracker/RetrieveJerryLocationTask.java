package com.edmundophie.jerrytracker;

import android.os.AsyncTask;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by edmundophie on 3/3/15.
 */
public class RetrieveJerryLocationTask extends AsyncTask<Void, Void, String> {
    private final String url = "http://167.205.32.46/pbd/api/track?nim=13512095";

    @Override
    protected String doInBackground(Void... params) {
        // Setting HTTP connection
        HttpClient client = new DefaultHttpClient();
        HttpHost proxy = new HttpHost("cache.itb.ac.id", 8080);
//        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpGet get = new HttpGet(url);
        HttpResponse resp = null;
        try {
            // Get response from server
            resp = client.execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            String buff = "";
            String result = "";
            while ((buff = reader.readLine()) != null)
                result += buff;
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // Parse JSON
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(result);
            MainActivity.jerryLocation.setLatitude(jsonObj.getDouble("lat"));
            MainActivity.jerryLocation.setLongitude(jsonObj.getDouble("long"));

            // Convert Epoch time
            long secondsSinceUnixEpoch = jsonObj.getLong("valid_until");
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            Date date = new Date(secondsSinceUnixEpoch*1000);
            String expDate = formatter.format(date);
            System.out.println("Expire : " + expDate);
            MainActivity.jerryLocation.setExpDate(expDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MainActivity.valLatitude.setText(MainActivity.jerryLocation.getLatitude().toString());
        MainActivity.valLongitude.setText(MainActivity.jerryLocation.getLongitude().toString());
        MainActivity.valExpire.setText(MainActivity.jerryLocation.getExpDate());
    }
}
