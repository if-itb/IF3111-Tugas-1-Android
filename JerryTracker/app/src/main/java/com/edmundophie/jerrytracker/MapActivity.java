package com.edmundophie.jerrytracker;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends ActionBarActivity implements LocationListener{
    private LatLng jerryLocation;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Create jerry marker on map
        jerryLocation = new LatLng(MainActivity.jerryLocation.getLatitude(), MainActivity.jerryLocation.getLongitude());
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragmentMap)).getMap();
        Marker jerry = map.addMarker(new MarkerOptions().position(jerryLocation));
        jerry.setTitle("Jerry's Position");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(jerryLocation, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Create my location marker on map
        map.setMyLocationEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        double myLat = location.getLatitude();
        double myLong = location.getLongitude();
        LatLng myLatLng = new LatLng(myLat, myLong);
        Marker myMarker = map.addMarker(new MarkerOptions().position(myLatLng));
        myMarker.setTitle("My Position");
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
//        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
