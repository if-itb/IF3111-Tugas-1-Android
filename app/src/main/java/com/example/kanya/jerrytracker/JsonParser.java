package com.example.kanya.jerrytracker;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.UnsupportedEncodingException;
        import java.util.ArrayList;
        import java.util.List;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.NameValuePair;
        import org.apache.http.StatusLine;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.entity.UrlEncodedFormEntity;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.message.BasicNameValuePair;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import android.util.Log;

public class JsonParser {

    final String TAG = "JsonParser.java";

    static InputStream iStream = null;
    static JSONObject jsonObject = null;
    static String json = "";

    public JSONObject getJSONFromUrl(String url) throws JSONException {

        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);

            HttpEntity httpEntity = response.getEntity();
            iStream = httpEntity.getContent();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse String to JSON object
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            iStream.close();
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonObject = new JSONObject(json);

        // return JSON Object
        return jsonObject;

    }

    public static JSONObject postData(String token) throws JSONException {
        String nim = "13512072";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

        try {
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("nim", nim));
            nameValuePair.add(new BasicNameValuePair("token", token));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity httpEntity = response.getEntity();
            iStream = httpEntity.getContent();

            //return response.getStatusLine().getStatusCode();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            iStream.close();
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonObject = new JSONObject(json);

        // return JSON Object
        return jsonObject;
    }
}