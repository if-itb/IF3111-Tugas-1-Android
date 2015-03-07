package com.example.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.widget.TextView;

public class HTTPRequestGet extends AsyncTask<String, Void, String> {
	private String jerry_loc;
	private TextView jerry;
	
	@Override
    protected String doInBackground(String... urls) {
		String response = "";
		for (String url : urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			try {
				HttpResponse execute = client.execute(httpGet);
				InputStream content = execute.getEntity().getContent();

				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
    }

    @Override
    protected void onPostExecute(String result) {
    	jerry.setText(result);
    }
}