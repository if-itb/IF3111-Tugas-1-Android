package com.gilang.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gilang.jerrytracker.R;

/**
 * Created by Gilang on 2/23/2015.
 */
public class ConnectionFragment extends Fragment{

    TextView text1;

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
        testVolley();
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

}
