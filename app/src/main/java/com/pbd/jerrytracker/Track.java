package com.pbd.jerrytracker;

import android.graphics.Camera;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Willy on 3/1/2015.
 */
public class Track extends AsyncTask<String, Void, LatLng> {
    private GoogleMap mMap;
    private Marker mMarker;

    public Track(GoogleMap _mMap) {
        mMap = _mMap;
        mMarker = _mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Jerry"));
    }

    @Override
    protected LatLng doInBackground(String[] params) {
        try {
            // create connection
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://167.205.32.46/pbd/api/track?nim="+params[0]);

            // get response
            HttpResponse response = client.execute(get);

            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            mMarker.remove();
                            new Track(mMap).execute("13512070", null, null);
                        }
                    },
                    json.getLong("valid_until")-(System.currentTimeMillis()/1000)
            );

            return new LatLng(json.getDouble("lat"), json.getDouble("long"));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(LatLng newPosition) {
        if (newPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, (float) 80.0));
            mMarker.setPosition(newPosition);
        }
    }
}