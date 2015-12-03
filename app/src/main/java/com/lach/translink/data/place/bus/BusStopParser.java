package com.lach.translink.data.place.bus;

public class BusStopParser {
    private static final String ENCODING_PREFIX = "#BUS_STOP:";

    public static String encodeBusStop(BusStop busStop) {
        return ENCODING_PREFIX + busStop.getId();
    }

    public static boolean isEncodedBusStop(String text) {
        if (text == null) {
            return false;
        }
        return text.startsWith(ENCODING_PREFIX);
    }

    public static BusStop decodeBusStop(BusStopDao busStopDao, String encodedString) {
        if (encodedString == null) {
            return null;
        }
        long busStopId = Long.parseLong(encodedString.replace(ENCODING_PREFIX, ""));
        return busStopDao.get(busStopId);
    }

    public static String prettyPrintBusStop(BusStopDao busStopDao, String encodedString) {
        if (encodedString == null) {
            return null;
        }
        BusStop busStop = decodeBusStop(busStopDao, encodedString);
        return "Bus stop: " + busStop.getStationId() + "\n" + busStop.getDescription();
    }

}
