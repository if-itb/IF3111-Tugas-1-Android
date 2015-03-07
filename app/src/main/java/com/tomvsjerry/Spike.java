package com.tomvsjerry;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Fahziar on 06/03/2015.
 */
public class Spike {
    private RequestQueue requestQueue;
    private boolean locationValid;
    private int nbRequest;
    private Context context;
    private SpikeListener listener;
    private double latitude;
    private double longitude;

    private String server = "http://167.205.32.46/pbd";
    private Date timeout;

    Spike(Context context, SpikeListener listener)
    {
        requestQueue = Volley.newRequestQueue(context);
        locationValid = false;
        nbRequest = 0;
        timeout = new Date();
        this.context = context;
        this.listener = listener;
    }

    public void askLocation(String nim)
    {
        if (nbRequest == 0) {
            JsonObjectRequest locationRequest = new JsonObjectRequest(Request.Method.GET, server + "/api/track?nim=" + nim, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("Success");
                            nbRequest--;
                            try {
                                latitude = response.getDouble("lat");
                                longitude = response.getDouble("long");
                                timeout.setTime(response.getLong("valid_until") * 1000);
                                System.out.println("Lat " + String.valueOf(latitude) + " Lon " + String.valueOf(longitude));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            listener.onAnswer();
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    nbRequest--;
                    if (error.networkResponse != null)
                    {
                        Toast.makeText(context, "Failed to ask jerry location:" + Integer.toString(error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to ask jerry location", Toast.LENGTH_LONG).show();
                    }
                }
            });

            //Send request
            requestQueue.add(locationRequest);
            nbRequest++;
        }
    }

    public void catchRequest(String nim, String token)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("nim", nim);
            data.put("token", token);
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest requestCatch = new JsonObjectRequest(Request.Method.POST, server + "/api/catch", data,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, "You succesfully catch Jerry", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null)
                {
                    Toast.makeText(context, "Failed to catch Jerry:" + Integer.toString(error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to catch Jerry", Toast.LENGTH_LONG).show();
                }
            }
        });

        requestQueue.add(requestCatch);
    }

    public boolean isLocationValid() {
        return locationValid;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Date getTimeout() {
        return timeout;
    }
}
