package com.example.networkconnection;

/**
 * Created by Kevin Zhong Local on 06/03/2015.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CatchWebPage extends AsyncTask<String, Void, String> {

    private TextView dataField;
    private Context context;
    private String token;

    public CatchWebPage(Context context,String token, TextView dataField) {
        this.context = context;
        this.dataField = dataField;
        this.token=token;
    }

    //check Internet conenction.
    private void checkInternetConenction() {
        ConnectivityManager check = (ConnectivityManager) this.context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (check != null) {
            NetworkInfo[] info = check.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Toast.makeText(context, "Internet is connected",
                                Toast.LENGTH_SHORT).show();
                    }

        } else {
            Toast.makeText(context, "not conencted to internet",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onPreExecute() {
        checkInternetConenction();
    }

    @Override
    protected String doInBackground(String... arg0) {
        String link = (String) arg0[0];
        Map<String, String> comment = new HashMap<String, String>();
        comment.put("nim","13512097");
        comment.put("token",this.token);
        HttpResponse httpResponse=null;
        HttpEntity httpEntity=null;
        String response=null;
        httpResponse=makeRequest(link,comment);
        httpEntity=httpResponse.getEntity();
        try {
            response= EntityUtils.toString(httpEntity);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return new String("IOException di doInBackGroung : " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result){
        this.dataField.setText(result);
    }

    public static HttpResponse makeRequest(String path, Map params){
        //instantiates httpclient to make request
        DefaultHttpClient httpclient = new DefaultHttpClient();

        //url with the post data
        HttpPost httpost = new HttpPost(path);

        //convert parameters into JSON object
        JSONObject holder = null;
        holder = new JSONObject(params);

        //passes the results to a string builder/entity
        StringEntity se = null;
        try {
            se = new StringEntity(holder.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //sets the post request as the resulting string
        httpost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");

        try {
            return httpclient.execute(httpost);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
