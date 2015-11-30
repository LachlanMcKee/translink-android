package com.lach.translink.data.location.favourite;

/**
 * Allows the user to save and load location data.
 */
public interface LocationFavourite {

    long getId();

    String getAddress();

    void setAddress(String address);
}