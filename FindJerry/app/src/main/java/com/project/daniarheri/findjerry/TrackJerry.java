package com.project.daniarheri.findjerry;

import android.os.AsyncTask;
import android.view.ViewStub;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by daniar heri on 3/3/2015.
 */
public class TrackJerry extends AsyncTask<String, String, String> {
    private Float lat;
    private Float lon;


    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    public void findJerry(String str) throws ExecutionException, InterruptedException {

        this.execute(str);
        String responseString = null;
        responseString = this.get();
        extractResponseString(responseString);
    }

    private void extractResponseString(String jsonStr) {
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String lantitude = jsonObj.getString("lat");
                String longitude = jsonObj.getString("long");
                String validTime = jsonObj.getString("valid_until");

                // convert date
                Long epoch = Long.parseLong(validTime);
                String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));
//
//                Toast toast = Toast.makeText(this, "Valid sampai :  " + date , Toast.LENGTH_LONG);
//                toast.show();
                lat = Float.parseFloat(lantitude);
                lon = Float.parseFloat(longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public LatLng getLatLing(){
        return new LatLng(lat,lon);
    }
}