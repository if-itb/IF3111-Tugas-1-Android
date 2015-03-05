package com.android.mario.getthejerry;

/**
 * Created by Mario on 06/03/2015.
 */
public class ViewPosition {
    private static int latitude, longitude;
    private static String validUntil, token;
    private static int status;

    public static int getStatus() {
        return status;
    }

    public static void setStatus(int status) {
        ViewPosition.status = status;
    }

    public static int getLatitude() {
        return latitude;
    }

    public static void setLatitude(int latitude) {
        ViewPosition.latitude = latitude;
    }

    public static int getLongitude() {
        return longitude;
    }

    public static void setLongitude(int longitude) {
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
