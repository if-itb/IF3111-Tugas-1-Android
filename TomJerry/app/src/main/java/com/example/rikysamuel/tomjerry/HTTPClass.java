package com.example.rikysamuel.tomjerry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rikysamuel on 3/6/2015.
 */
public class HTTPClass {
    private String url;
    private String contents;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String doGet(){
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response;
        String result = "";
        try {
            response = client.execute(request);

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return result;
    }

    public String doPost(){
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        String result = "";

        try {
            List<NameValuePair> obj = new ArrayList<>();
            obj.add(new BasicNameValuePair("nim","13512089"));
            obj.add(new BasicNameValuePair("token",contents));

            post.setEntity(new UrlEncodedFormEntity(obj));
            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();


            StringBuilder bd = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";

            while ((line = rd.readLine()) != null) {
                bd.append(line);
            }
            result = bd.toString();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
}
