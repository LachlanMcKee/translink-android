package com.lach.translink.data.place.bus;

public interface BusStop {
    long getId();

    String getStationId();

    void setStationId(String stationId);

    String getDescription();

    void setDescription(String description);

    double getLatitude();

    void setLatitude(double latitude);

    double getLongitude();

    void setLongitude(double longitude);
}
