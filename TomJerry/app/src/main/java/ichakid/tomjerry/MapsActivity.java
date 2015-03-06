package ichakid.tomjerry;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MapsActivity extends Activity {
    private LatLng JERRY;
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try {
            setUpMapIfNeeded();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Memindahkan kamera langsung ke jerry dengan zoom 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JERRY, 15));
        //Zoom in, animasi kameranya
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setUpMapIfNeeded();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    private void setUpMapIfNeeded() throws ExecutionException, InterruptedException {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
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
    private void setUpMap() throws ExecutionException, InterruptedException {
        getJerryPosition();
        Marker jerry = mMap.addMarker(new MarkerOptions().position(JERRY).title("Jerry"));
        Marker kiel = mMap.addMarker(new MarkerOptions()
                .position(KIEL)
                .title("Kiel")
                .snippet("Kiel is cool")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));
    }

    public void getJerryPosition() throws ExecutionException, InterruptedException {
        RequestTask req = new RequestTask();
        req.execute("http://167.205.32.46/pbd/api/track?nim=13512084");
        String response = (String) req.get();
//        LatLng loc = null;
        try {
            JSONObject json = new JSONObject(response);
            String lat = json.getString("lat");
            String lon = json.getString("long");
            String valid_until = json.getString("valid_until");
//            loc = new LatLng(Float.parseFloat(lat), Float.parseFloat(lon));
            JERRY = new LatLng(Float.parseFloat(lat), Float.parseFloat(lon));

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
