package adwisatya.jerrytracker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by adwisaty4 on 3/4/2015.
 */
public class GoogleMap {

    public void updatePosition(GoogleMap gmap, LatLng latLng) {
        /*try {
            gmap = ((MapFragment) getFragmentManager().
                    findFragmentById(R.id.map)).getMap();
            gmap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
            Marker TP = gmap.addMarker(new MarkerOptions().
                    position(latLng).title("JerryLocation"));
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
