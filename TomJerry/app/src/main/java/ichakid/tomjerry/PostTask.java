package ichakid.tomjerry;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 3/7/2015.
 */
public class PostTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost();
        HttpResponse response = null;
        String responseString = "";
        URI website = null;
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("token", params[1].toString()));
        nameValuePair.add(new BasicNameValuePair("nim", "13512084"));

        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePair));
            website = new URI(params[0].toString());
            request.setURI(website);
            response = httpClient.execute(request);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            responseString = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return responseString;
    }
}
