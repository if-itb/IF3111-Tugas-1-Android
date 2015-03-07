package com.example.networkconnection;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

/**
 * Created by Kevin Zhong Local on 07/03/2015.
 */
public class Map extends Activity{

    //@SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        showMap();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map,menu);
        return true;
    }*/

    public void showMap(){
        String urltrack = "http://167.205.32.46/pbd/api/track?nim=13512097";
        DownloadWebPage hasiltrack= new DownloadWebPage(this);
        hasiltrack.execute(urltrack);
        try {
            JSONObject mainObj = new JSONObject(hasiltrack.get().toString());
            Double Lat= mainObj.getDouble("lat");
            Double Longitude = mainObj.getDouble("long");
            String ED = mainObj.getString("valid_until");
            final LatLng KolamIntel = new LatLng(Lat,Longitude);
            GoogleMap googleMap = null;
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(KolamIntel).title("PosisiJerrySkrg"));

            googleMap.getUiSettings().setZoomGesturesEnabled(true);


            Log.i(DownloadWebPage.class.getName(), hasiltrack.get().toString());
            Log.i(DownloadWebPage.class.getName(), Lat + " " + Longitude + " " + ED);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        Log.i("?icon","is selected?");
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Map.this.finish();
    }*/

}