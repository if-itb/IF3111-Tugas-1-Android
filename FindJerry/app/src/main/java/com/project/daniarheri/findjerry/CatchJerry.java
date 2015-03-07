package com.project.daniarheri.findjerry;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by daniar heri on 3/3/2015.
 */
public class CatchJerry extends AsyncTask<String, String, String> {
    private Float lat;
    private Float lon;
    private String secretToken;
    private String responseString;
    private boolean success = false;
    private final String URL = "http://167.205.32.46/pbd/api/catch";
    public Integer status;
    @Override
    protected String doInBackground(String... params) {
        HttpClient httpClient = new DefaultHttpClient();
        String response = "";
        HttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(URL);
        try {
            List<BasicNameValuePair> Parameters = new ArrayList();
            Parameters.add(new BasicNameValuePair("nim","13512064"));
            Parameters.add(new BasicNameValuePair("token",secretToken));
            httpPost.setEntity(new UrlEncodedFormEntity(Parameters));
            Log.d("Post Parameters", httpPost.getParams().toString());
            httpResponse = httpClient.execute(httpPost);
            InputStream content = httpResponse.getEntity().getContent();
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
        Log.d("Poster",secretToken);
        Log.d("Poster", response);
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    public void catchJerry(String token) throws ExecutionException, InterruptedException {
        secretToken = token;
        this.execute();
        extractResponseString(this.get());
    }

    private void extractResponseString(String jsonStr) {
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String message = jsonObj.getString("message");
                status = jsonObj.getInt("code");

                if(status==200){
                    success = true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean IsSuccess(){
        return success;
    }
}