package com.yanfa.jerrysearcher;

import java.util.Date;

/**
 * Created by Yanfa on 04/03/2015.
 */
public class Data {
    private String lat;
    private String lon;
    private String valid_until;
    public static Date validTil;

    Data(){

    }

    Data(String lat, String lon, String valid_until){
        this.lat = lat;
        this.lon = lon;
        this.valid_until = valid_until;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getValid_until() {
        return valid_until;
    }

    public void setValid_until(String valid_until) {
        this.valid_until = valid_until;
    }

    @Override
    public String toString() {
        return "lat :"+lat
                +"\n long :" + lon
                +"\n valid_until :" + valid_until;
    }
}
