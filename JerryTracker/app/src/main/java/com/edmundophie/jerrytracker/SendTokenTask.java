package com.edmundophie.jerrytracker;

import android.os.AsyncTask;
import android.preference.PreferenceActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edmundophie on 3/4/15.
 */
public class SendTokenTask extends AsyncTask<String, Void, String> {
    private final String url = "http://167.205.32.46/pbd/api/catch";

    @Override
    protected String doInBackground(String... params) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        HttpResponse resp;
        try {
            post.setEntity(new StringEntity("{\"nim\":\"13512095\",\"token\":\""+params[0]+"\"}"));
            post.setHeader("Content-type", "application/json");
            resp = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            String buff = "";
            String result = "";
            while((buff = reader.readLine()) != null)
                result += buff;
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        String message = null;
        String code = null;
        try {
            JSONObject jsonObj = new JSONObject(result);
            message = jsonObj.getString("message");
            code = jsonObj.getString("code");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        MainActivity.valResponse.setText(code + " : " + message);
    }
}
