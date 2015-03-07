package com.muhammadhusain.jerrytracker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceHandler {

    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;

    public ServiceHandler() {

    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String url, int method,
                                  List<NameValuePair> params) {
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch/");
                // adding post params
                if (params != null) {
                    String json ="";
                    JSONObject jsonObject = new JSONObject();
                    for (int i=0;i<params.size();i++){
                        jsonObject.accumulate(params.get(i).getName(), params.get(i).getValue());
                    }
                    json = jsonObject.toString();
                    StringEntity strEntity = new StringEntity(json);
                    httpPost.setEntity(strEntity);
                }
                httpPost.setHeader("Content-type","application/json");
                httpResponse = httpClient.execute(httpPost);

            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);

                httpResponse = httpClient.execute(httpGet);

            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;

    }
}
    /**
HttpClient client = new DefaultHttpClient();
String p_result = null;
try {
        HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
        JSONObject json = new JSONObject();
        json.put("nim", "13512067");
        json.put("token", contents);
        StringEntity se = new StringEntity(json.toString());
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        post.setEntity(se);
        HttpResponse response = client.execute(post);
        if (response != null) {
        HttpEntity entity = response.getEntity();
        BufferedHttpEntity buffEntity = new BufferedHttpEntity(entity);
        BufferedReader rd = new BufferedReader(new InputStreamReader(buffEntity.getContent()));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
        sb.append(line);
        }
        p_result = sb.toString();
        }

        } catch (JSONException | IOException e) {
        e.printStackTrace();
        }

        String message = "";
        int code = 0;
        String title = "Result";
        try {
        JSONObject json = new JSONObject(p_result);
        message = json.getString("message");
        code = Integer.parseInt(json.getString("code"));
        if(code == 200){
        title = "Success!";
        }
        else if (code == 400){
        title = "Missing Parameter";
        }
        else if (code == 403){
        title = "Forbidden";
        }
        AlertDialog alertDialog = new AlertDialog.Builder(Barcode.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(p_result);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int which) {
        }
        });
        alertDialog.show();

        } catch (JSONException e) {
        e.printStackTrace();
        }
**/