package com.lach.translink.data.place;

public interface BusStop {
    long getId();

    long getTranslinkId();

    void setTranslinkId(long translinkId);

    String getDescription();

    void setDescription(String description);

    double getLatitude();

    void setLatitude(double latitude);

    double getLongitude();

    void setLongitude(double longitude);
}
