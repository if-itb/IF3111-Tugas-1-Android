package com.android.mario.getthejerry;

/**
 * Created by Mario on 07/03/2015.
 */
public class ViewPosition {
    private static double latitude;
    private static double longitude;
    private static String validUntil, token;
    private static int status;

    public static int getStatus() {
        return status;
    }

    public static void setStatus(int status) {
        ViewPosition.status = status;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        ViewPosition.latitude = latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        ViewPosition.longitude = longitude;
    }

    public static String getValidUntil() {
        return validUntil;
    }

    public static void setValidUntil(String validUntil) {
        ViewPosition.validUntil = validUntil;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        ViewPosition.token = token;
    }

}
