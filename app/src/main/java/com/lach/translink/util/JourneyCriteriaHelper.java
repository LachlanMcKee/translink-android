package com.lach.translink.util;

import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JourneyCriteriaHelper {
    public static String createJourneyDescription(JourneyCriteria search) {
        String journeyDescription = "";

        JourneyTimeCriteria timeType = search.getJourneyTimeCriteria();
        if (timeType != null) {
            journeyDescription += timeType.toString();
        }

        Date time = search.getTime();
        if (time != null) {
            // Don't show the time for First and Last trips.
            if (timeType == null || (timeType != JourneyTimeCriteria.FirstTrip && timeType != JourneyTimeCriteria.LastTrip)) {
                DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
                journeyDescription += " " + timeFormat.format(time);
            }
        }

        JourneyTransport transport = search.getJourneyTransport();
        if (transport != null) {
            if (journeyDescription.length() > 0) {
                journeyDescription += " via";
            }
            journeyDescription += " " + transport.toString();
        }

        return journeyDescription;
    }
}
