package binangkit.lingga.jelink.tomandjerry;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {
    LatLng Jerry = new LatLng(1, 1);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    ActionBarActivity myContext;
    Marker jerryMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        //onclick dll tulis di sini

        //setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        //setUpMap();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
            SupportMapFragment smf = ((SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.mymap));
            if(smf==null) Log.d("debug", "context null");
            mMap = smf.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near ITB Bandung.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        jerryMarker = mMap.addMarker(new MarkerOptions()
                .position(Jerry)
                .title("Jerry")
                .snippet("I'm here now! Come and get me!")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.cursor_jerry)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Jerry, 16.3f));
        mMap.setMyLocationEnabled(true);
    }

    public void setJerry(double lat, double lng) {
        jerryMarker.remove();
        Jerry = new LatLng(lat, lng);
        setUpMap();
    }
}
