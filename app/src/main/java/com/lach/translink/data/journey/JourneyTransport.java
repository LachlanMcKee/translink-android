package com.lach.translink.data.journey;

/**
 * The journey transport types which correlates with the Translink journey transport types.
 */
public enum JourneyTransport {
    All("Any Vehicle"), Bus("Bus"), Train("Train"), Tram("Tram"), Ferry("Ferry");

    private final String type;

    JourneyTransport(String aType) {
        type = aType;
    }

    @Override
    public String toString() {
        return type;
    }
}
