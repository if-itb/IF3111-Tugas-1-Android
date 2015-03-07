package rasyad.carijerry;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

/**
 * Created by Reno on 3/7/2015.
 */
public class GMaps extends Fragment {
    LatLng Jerry = new LatLng(1,1);
    private GoogleMap mMap; //null kalau APK Google Play services tidak ada
    ActionBarActivity myContext;
    Marker jerryMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_main, container, false);

        //setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        return view;
    }

    @Override
    public void onAttach(Activity activity){
        myContext = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment smf = ((SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.map));
            if(smf==null) Log.d("debug", "context null");
            mMap = smf.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we just add marker near location
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        jerryMarker = mMap.addMarker(new MarkerOptions()
                .position(Jerry)
                .title("Jerry")
                .snippet("Aku disini")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Jerry, 16.3f));
        mMap.setMyLocationEnabled(true);
    }

    public void setJerry(double lat, double lng) {
        jerryMarker.remove();
        Jerry = new LatLng(lat, lng);
        setUpMap();
    }
}
