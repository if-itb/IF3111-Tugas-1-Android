package com.tracker.timothypratama.tomandjerryapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tracker.timothypratama.tomandjerryapplication.Model.TrackJerryViewModel;
import com.tracker.timothypratama.tomandjerryapplication.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class QRCodeScanner extends ActionBarActivity {

    private final String URL = "http://167.205.32.46/pbd/api/catch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                TrackJerryViewModel.setSecret_token(contents);
                Log.d("Contents",contents);

                Catcher catcher = new Catcher();
                catcher.execute();
            }
            if(resultCode == RESULT_CANCELED) {
                //do nothing
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_qrcode_scanner, menu);
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

    public void catchjerry(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE MODE");
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    class Catcher extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            String response = "";
            HttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost(URL);
            try {
                List<BasicNameValuePair> Parameters = new ArrayList();
                Parameters.add(new BasicNameValuePair("nim","13512032"));
                Parameters.add(new BasicNameValuePair("token",TrackJerryViewModel.getSecret_token()));
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
            Log.d("Poster",TrackJerryViewModel.getSecret_token());
            Log.d("Poster", response);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView t_result = (TextView) findViewById(R.id.resultTextView);
            TextView t_status = (TextView) findViewById(R.id.StatusTextView);
            try {
                JSONObject object = new JSONObject(s);
                String result = object.getString("message");
                int status = object.getInt("code");
                TrackJerryViewModel.setHttpPostStatus(status);
                Log.d("Poster","status: " + status);
                Log.d("Poster","message: " + result);
                t_result.setText(result);
                t_status.setText(String.valueOf(status));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
