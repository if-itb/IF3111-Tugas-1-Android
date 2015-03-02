package com.tracker.timothypratama.tomandjerryapplication.Activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tracker.timothypratama.tomandjerryapplication.Model.TrackJerryViewModel;
import com.tracker.timothypratama.tomandjerryapplication.R;

public class GPSTracking extends ActionBarActivity implements OnMapReadyCallback {

    private GoogleMap gm;
    private final int zoom = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracking);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gpstracking, menu);
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
    public void onMapReady(GoogleMap googleMap) {
        LatLng jerry = new LatLng(TrackJerryViewModel.getLatitude(), TrackJerryViewModel.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jerry,zoom));
        googleMap.addMarker(new MarkerOptions()
        .title("Jerry")
        .snippet("Jerry is here! Hurry up!")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.jerry))
        .anchor(0.5f,1.0f)
        .position(jerry));
        gm = googleMap;
    }

    public void showJerry(View view) {
        LatLng jerry = new LatLng(TrackJerryViewModel.getLatitude(), TrackJerryViewModel.getLongitude());
        gm.animateCamera(CameraUpdateFactory.newLatLngZoom(jerry,zoom));
    }
}
