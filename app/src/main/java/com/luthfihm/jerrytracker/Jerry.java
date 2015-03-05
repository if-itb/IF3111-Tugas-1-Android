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
    private long valid;

    public Jerry(){
        latLng = new LatLng(0,0);
        valid = 0;//new Timestamp(0);
    }

    public void update(String json)
    {
        try
        {
            JSONObject obj = new JSONObject(json);
            double latitude = obj.getDouble("lat");
            // Get longitude of the current location
            double longitude = obj.getDouble("long");
            latLng = new LatLng(latitude,longitude);
            //valid = new Timestamp(obj.getLong("valid_until"));
            valid = obj.getLong("valid_until")*1000;
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
        SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
        return sdf1.format(new Date(valid));
    }
}
