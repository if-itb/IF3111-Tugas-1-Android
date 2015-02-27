package com.gilang.fragment;


import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gilang.jerrytracker.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gilang on 2/23/2015.
 */
public class ConnectionFragment extends Fragment{

    TextView text1;
    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";
    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    public ConnectionFragment(){

    }

    public static ConnectionFragment newInstance(){
        ConnectionFragment connectionFragment = new ConnectionFragment();
        return connectionFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_main, parent, false);
        text1 = (TextView) v.findViewById(R.id.text1);
        testCatch();
        return v;
    }

    public void testVolley(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="http://167.205.32.46/pbd/api/track?nim=13512045";

        // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                // Display the first 500 characters of the response string.
                                text1.setText("Response is: "+ response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                text1.setText("That didn't work!");
                            }
                        });
        // Add the request to the RequestQueue.
                queue.add(stringRequest);
    }

    public void testCatch(){
        String url = "http://167.205.32.46/pbd/api/catch";
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject obj = new JSONObject();
        try {
            obj.put("nim", "13512045");
            obj.put("token", "tomandjerry");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("success" + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("error" + error.toString());
                    }
                }
                ){

            @Override
            public byte[] getBody() throws AuthFailureError {
                return obj.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(req);

    }

}
