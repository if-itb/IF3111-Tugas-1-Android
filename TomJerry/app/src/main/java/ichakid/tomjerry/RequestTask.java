package ichakid.tomjerry;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by User on 3/6/2015.
 */
public class RequestTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet request = new HttpGet();
        HttpResponse response = null;
        URI website = null;
        BufferedReader in = null;
        String responseString = "";
        try {
            website = new URI(params[0].toString());
            request.setURI(website);
            response = httpclient.execute(request);
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
