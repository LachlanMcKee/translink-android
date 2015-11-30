package com.lach.translink.util;

public class LocationUtil {
    public static String getLocationInformation(String locationInformation) {
        String locationString = locationInformation.replace("(Location)", "");
        locationString = locationString.replace("(Landmark)", "");
        locationString = locationString.replace("(Stop)", "");
        locationString = locationString.replace("(Suburb)", "");

        locationString = locationString.replace("opposite", "Opp");
        locationString = locationString.replace("approaching", "App");
        locationString = locationString.replace("far side of", "F/S");

        return locationString.trim();
    }
}
