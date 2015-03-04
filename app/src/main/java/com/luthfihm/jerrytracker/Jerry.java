package com.luthfihm.jerrytracker;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by luthfi on 04/03/2015.
 */
public class Jerry {
    private LatLng latLng;
    private Timestamp valid;

    public Jerry(String nim) throws JSONException {
        try
        {
            String json = "{\"lat\":\"-6.891278\",\"lon\":\"107.610255\",\"valid_until\":1425405600}";
            JSONObject obj = new JSONObject(json);
            double latitude = obj.getDouble("lat");
            // Get longitude of the current location
            double longitude = obj.getDouble("lon");
            latLng = new LatLng(latitude,longitude);
            valid = new Timestamp(obj.getLong("valid_until"));
        }
        catch (JSONException e)
        {

        }
    }

    public LatLng getLatLng()
    {
        return latLng;
    }

    public String getTime()
    {
        String format = "d MMM yyyy, HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(new Date(valid.getTime()));
    }
}
