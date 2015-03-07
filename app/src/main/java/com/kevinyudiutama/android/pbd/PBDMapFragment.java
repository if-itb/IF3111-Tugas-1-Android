package com.kevinyudiutama.android.pbd;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by kevinyu on 3/2/15.
 */
public class PBDMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String GET_API_URL = "http://167.205.32.46/pbd/api/track?nim=";
    private static final String LAT_KEY = "lat";
    private static final String LONG_KEY = "long";
    private static final String VALID_UNTIL_KEY = "valid_until";
    private static final String DESTINATION_TAG = "destination";
    private static final String NIM_KEY = "nim";
    private static final String LAST_NIM_KEY = "last_nim";

    private float mLongitude = 0;
    private float mLatitude = 0;
    private long mValidUntil;
    private String mNIM;
    private String mLastNIM;

    RequestQueue mRequestQueue;
    CountDownTimer mCountDownTimer;

    MapView mapView;
    TextView mNotificationTextView;
    ImageButton mRefreshButton;

    GoogleMap mMap;

    Marker mDestinationMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pbd_map, container, false);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        mapView = (MapView) v.findViewById(R.id.mapview);
        mNotificationTextView = (TextView) v.findViewById(R.id.seconds_left_textView);
        mRefreshButton = (ImageButton) v.findViewById(R.id.refresh_button);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDestinationLocation();
            }
        });

        View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);

        return v;
    }


    public void onRestart() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mNIM = pref.getString(NIM_KEY,"13512010");
        updateDestinationMarker();
    }

    @Override
    public void onResume() {
        Log.d("DEBUG","onResume()");
        mapView.onResume();
        super.onResume();

        checkLocationEnabled();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLatitude = pref.getFloat(LAT_KEY,0f);
        mLongitude = pref.getFloat(LONG_KEY,0f);
        mValidUntil = pref.getLong(VALID_UNTIL_KEY,0);
        mNIM = pref.getString(NIM_KEY,"13512010");
        mLastNIM = pref.getString(LAST_NIM_KEY,"13512010");

    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(LAT_KEY,mLatitude);
        editor.putFloat(LONG_KEY,mLongitude);
        editor.putLong(VALID_UNTIL_KEY,mValidUntil);
        editor.putString(LAST_NIM_KEY,mLastNIM);
        editor.commit();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue!=null) {
            mRequestQueue.cancelAll(DESTINATION_TAG);
        }
        if (mCountDownTimer!=null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);

        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(
                    new LatLng(mLatitude,mLongitude)).
                        title("Jerry Location"));

        updateDestinationMarker();

    }

    protected void getDestinationLocation() {
        Log.d("DEBUG","getDestinationLocation()");
        String URLString = GET_API_URL + mNIM;
        Log.d("DEBUG",URLString);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                URLString,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d("DEBUG","onResponse()");
                        try {
                            mLatitude = (float)jsonObject.getDouble(LAT_KEY);
                            mLongitude = (float)jsonObject.getDouble(LONG_KEY);
                            mValidUntil = jsonObject.getLong(VALID_UNTIL_KEY);
                            mLastNIM = mNIM;
                            updateDestinationMarker();
                        } catch (Exception e) {
                            Log.d("DEBUG","error json parse volley");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("DEBUG",volleyError.getMessage());
                mNotificationTextView.setText(R.string.no_internet_connection);
            }
        }){
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, "UTF-8");
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        jsonObjectRequest.setTag(DESTINATION_TAG);
        jsonObjectRequest.setShouldCache(false);
        mRequestQueue.add(jsonObjectRequest);

    }


    private void updateDestinationMarker() {
        Log.d("DEBUG","updateDestinationMarker()");
        Log.d("DEBUG",mNIM+" "+mLastNIM);
        mDestinationMarker.setPosition(new LatLng(mLatitude, mLongitude));
        if (!mLastNIM.equals(mNIM)) {
            getDestinationLocation();
            return;
        }
        if (System.currentTimeMillis()/1000<mValidUntil) {
            mCountDownTimer = new CountDownTimer(mValidUntil*1000 - System.currentTimeMillis(),1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mNotificationTextView.setText(String.format(
                            getActivity().
                                    getResources().
                                    getString(R.string.location_uptodate),
                            Long.toString(millisUntilFinished/1000)));
                }

                @Override
                public void onFinish() {
                    getDestinationLocation();
                }
            }.start();
        } else {
            getDestinationLocation();
        }

    }

    private void checkLocationEnabled() {
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;
        if(lm==null)
            lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}

        if(!gps_enabled && !network_enabled){
            mNotificationTextView.setText(R.string.gps_not_enabled);
        }
    }

}
