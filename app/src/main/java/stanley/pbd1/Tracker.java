package stanley.pbd1;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Tracker extends ActionBarActivity  {
    String textview;
    LatLng jerry = new LatLng(0,0);
    JSONObject jObject;
    public GoogleMap jerrymap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        new AsyncConnection().execute("http://167.205.32.46/pbd/api/track?nim=13512086");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        jerrymap= mapFragment.getMap();
        jerrymap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public class AsyncConnection extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... urls) {
            try {
                URL url = new URL (urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();
                int stats = connection.getResponseCode();
                if (stats==200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return parseJSON(sb.toString());
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(LatLng coord) {
            jerry = coord;
            jerrymap.setMyLocationEnabled(true);
            jerrymap.moveCamera(CameraUpdateFactory.newLatLngZoom(jerry, 13));
            jerrymap.addMarker(new MarkerOptions()
                    .title("Jerry")
                    .snippet("Jerry is here")
                    .position(jerry));
        }

        public LatLng parseJSON(String jsonstring){
            try {
                JSONObject jObject = new JSONObject(jsonstring);
                String longi = jObject.getString("long");
                String lati = jObject.getString("lat");
                double lng = Double.parseDouble(longi);
                double lat = Double.parseDouble(lati);
                LatLng retval = new LatLng(lat,lng);
                return retval;
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
