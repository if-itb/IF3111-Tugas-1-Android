package com.yanfa.jerrysearcher;

import android.app.*;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yanfa on 03/03/2015.
 */
public class MapFragment extends Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private FragmentActivity myContext;
    public static LatLng langTut;
    private String jsonRet;
    private Data data;
    private Button refButton;

    public MapFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);
        loadMap();
        refButton = (Button)rootView.findViewById(R.id.refreshButton);


        refButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                long seconds = c.get(Calendar.SECOND);
                if (seconds > Data.validTil.getTime()){
                    MainActivity.loadBar.setVisibility(View.VISIBLE);
                    loadMap();
                }
                else{
                    Toast.makeText(getActivity(),"Masih Terupdate", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment)myContext.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            mMap.setMapType(1);
            //Log.d("map", "masuk sebelum null");
            if (mMap != null) {
                //Log.d("map", "masuk != null");
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //Log.d("json", "masuk sini " + langTut);
        Marker itb = mMap.addMarker(new MarkerOptions()
                .position(langTut)
                .title("Jerry")
                .snippet("Jerry Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(langTut, 16));
    }

    public void loadMap(){
        final Gson gson = new Gson();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="http://167.205.32.46/pbd/api/track?nim=13512037";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //langTut.setText("Response is: "+ response);
                        //Log.d("json",response.substring(3,response.length()));
                        jsonRet = "" + response.substring(3,response.length()).replace("long", "lon");
                        data = gson.fromJson(jsonRet, Data.class);
                        langTut = new LatLng(Float.parseFloat(data.getLat()), Float.parseFloat(data.getLon()));
                        Data.validTil = new Date(Long.parseLong(data.getValid_until()));
                        setUpMapIfNeeded();
                        MainActivity.loadBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

}
