package com.tracker.timothypratama.tomandjerryapplication.Model;

/**
 * Created by timothy.pratama on 3/2/2015.
 */
public class TrackJerryViewModel {
    private static double longitude;
    private static double latitude;

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
}
