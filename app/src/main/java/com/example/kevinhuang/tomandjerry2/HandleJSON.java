package com.example.kevinhuang.tomandjerry2;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Kevin Huang on 3/6/2015.
 */
public class HandleJSON {
    private String latitude = "lan";
    private String longitude = "long";
    private String valid = "valid_until";
    private String urlString = null;

    public volatile boolean parsingComplete = true;
    public HandleJSON(String url){
        this.urlString = url;
    }
    public String getLatitude(){
        return latitude;
    }
    public String getLongitude(){
        return longitude;
    }
    public String getValid(){
        return valid;
    }
    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);

            latitude = reader.getString("lat");
            longitude = reader.getString("long");
            valid = unixToDate(reader.getString("valid_until"));
            parsingComplete = false;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void fetchJSON(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    Log.d("Thread", "Creating new thread");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();
                    String data = convertStreamToString(stream);
                    readAndParseJSON(data);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    private String unixToDate(String unix_timestamp) {
        long timestamp = Long.parseLong(unix_timestamp) * 1000;

        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        sdf.setTimeZone(timeZone);
        String date = sdf.format(timestamp);

        return date.toString();
    }
}

