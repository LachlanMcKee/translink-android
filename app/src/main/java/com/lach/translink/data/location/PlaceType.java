package com.lach.translink.data.location;

/**
 * Defines a whether a place is of type {@link PlaceType#TO} or {@link PlaceType#FROM}
 */
public enum PlaceType {
    TO("Destination"), FROM("Starting Point");

    PlaceType(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }
}