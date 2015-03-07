package phone.pbd;

import java.util.Date;

/**
 * Created by WILLY on 3/4/2015.
 */
public class Position {
    private double latitude, longitude;
    private long valUntil;

    public Position(double latitude, double longitude, long valUntil) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.valUntil = valUntil;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Date getValidUntil() {
//        String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date (this.valUntil*1000));
//        return new Date(System.currentTimeMillis() + 5000);
        return new Date(this.valUntil * 1000);
    }

    public long getValidUntilInDouble() {
        return this.valUntil;
    }

    public boolean isStillValid() {
        return (this.getValidUntilInDouble() - System.currentTimeMillis() > 0);
    }

    public long getValUntilRemainingInMilliseconds() {
        return (this.getValidUntilInDouble() - System.currentTimeMillis());
    }
}
