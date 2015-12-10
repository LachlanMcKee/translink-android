package com.lach.common.data.map;

public final class MapPosition {
    public final double latitude;
    public final double longitude;

    public MapPosition(double latitude, double longitude) {
        // Taken from the Google Maps LatLng class.
        if(-180.0D <= longitude && longitude < 180.0D) {
            this.longitude = longitude;
        } else {
            this.longitude = ((longitude - 180.0D) % 360.0D + 360.0D) % 360.0D - 180.0D;
        }

        this.latitude = Math.max(-90.0D, Math.min(90.0D, latitude));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
