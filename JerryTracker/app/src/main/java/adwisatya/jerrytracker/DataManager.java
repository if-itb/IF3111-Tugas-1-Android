package adwisatya.jerrytracker;

/**
 * Created by adwisaty4 on 3/2/2015.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class DataManager {
    HttpClient httpClient = new DefaultHttpClient();

    String getLocation(){
        String location = "";
        HttpGet httpGet  = new HttpGet("http://167.205.32.33/pbd");
        try{
            httpGet.setHeader("nim", "13512043");
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            HttpResponse response = httpClient.execute(httpGet);
            Log.d("HttpResponse:", response.toString());
            location = response.toString();
        }catch(ClientProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return location;
    }
}
