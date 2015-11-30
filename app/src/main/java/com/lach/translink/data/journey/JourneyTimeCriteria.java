package com.lach.translink.data.journey;

/**
 * The time criteria which correlates with the Translink time types.
 */
public enum JourneyTimeCriteria {
    LeaveAfter("Leave After"), ArriveBefore("Arrive Before"), FirstTrip("First Trip"), LastTrip("Last Trip");

    private final String type;

    JourneyTimeCriteria(String aType) {
        type = aType;
    }

    @Override
    public String toString() {
        return type;
    }
}
