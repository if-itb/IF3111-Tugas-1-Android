package com.tracker.timothypratama.tomandjerryapplication.Model;

/**
 * Created by timothy.pratama on 3/2/2015.
 */
public class TrackJerryViewModel {
    private static double longitude;
    private static double latitude;
    private static String valid_until;
    private static String secret_token;
    private static int httpPostStatus;

    public static int getHttpPostStatus() {
        return httpPostStatus;
    }

    public static void setHttpPostStatus(int httpPostStatus) {
        TrackJerryViewModel.httpPostStatus = httpPostStatus;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        TrackJerryViewModel.longitude = longitude;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        TrackJerryViewModel.latitude = latitude;
    }

    public static String getValid_until() {
        return valid_until;
    }

    public static void setValid_until(String valid_until) {
        TrackJerryViewModel.valid_until = valid_until;
    }

    public static String getSecret_token() {
        return secret_token;
    }

    public static void setSecret_token(String secret_token) {
        TrackJerryViewModel.secret_token = secret_token;
    }
}
