package com.pbd.jerrytracker;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Arrays;

/**
 * Created by Willy on 3/1/2015.
 */
public class Catch extends AsyncTask<String, Void, String> {
    private Context mContext;

    public Catch(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");

            JSONObject json = new JSONObject();
            json.put("nim", 13512070);
            json.put("token", params[0]);

            HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
            post.setEntity(entity);
            HttpResponse response = client.execute(post);

            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "No Internet Connection";
    }

    protected void onPostExecute(String content) {
        try{
            JSONObject json = new JSONObject(content);
            Toast.makeText(mContext, json.getString("code")+"\n"+json.getString("message"), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}