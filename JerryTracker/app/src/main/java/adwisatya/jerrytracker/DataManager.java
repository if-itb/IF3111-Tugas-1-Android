package adwisatya.jerrytracker;

/**
 * Created by adwisaty4 on 3/2/2015.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DataManager extends AsyncTask {

    @Override
    protected Object doInBackground(Object... arg0) {
        return getLocation();
    }

    public String getLocation(){
        String location = "";
        HttpClient client = new DefaultHttpClient();
        HttpGet request =  new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512043");
        HttpResponse response;
        try{
            response = client.execute(request);
            location = response.getEntity().toString();

        }catch(Exception e){
            e.printStackTrace();
        }
        return location;
    }
}
