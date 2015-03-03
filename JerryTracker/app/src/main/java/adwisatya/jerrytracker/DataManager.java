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
import android.os.Bundle;
import android.util.Log;
public class DataManager {
    public String getLocation(){

        String location = "1";
        HttpClient Client = new DefaultHttpClient();


        try{
            HttpGet httpGet =  new HttpGet("http://itb.ac.id");
            HttpResponse response = Client.execute(httpGet);
            BufferedReader rd = new BufferedReader
                    (new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                location = location + line;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return location;

    }
}
