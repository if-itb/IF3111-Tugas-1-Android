package com.example.kevinhuang.tomandjerry2;

/**
 * Created by Kevin Huang on 3/6/2015.
 */
public class JerryLocation {
    private static double latitude = 0.0;
    private static double longitude= 0.0;
    private static long valid_time = 1;
    public double GetLatitude(){
        return latitude;
    }
    public double GetLongitude() {
        return longitude;
    }
    public double GetTime(){return valid_time;}
    public void setLatitude(double x){
        latitude = x;
    }
    public void setLongitude(double x){
        longitude = x;
    }
    public void setTime(long x){valid_time = x;}
}
