package com.example.andariassilvanus.tomandjerry;

import android.util.Log;

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

/**
 * Created by Andarias Silvanus on 15/03/06.
 */
public class HTTP_Request {
    public String getJerry() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("167.205.32.46/pbd/api/track?nim=13512022");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // If Status = "OK"
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
            else {
            }
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Isi GET: "+builder.toString());
        return builder.toString();
    }

    public void postToken() {
        JSONObject object = new JSONObject();
        try {
            object.put("nim", "13512022");
            object.put("token", "SECRET_TOKEN_HERE");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(object);
    }
}
