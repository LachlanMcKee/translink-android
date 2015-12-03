package com.lach.translink.data.place;

import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopDao;
import com.lach.translink.data.place.bus.BusStopParser;

public class PlaceParser {

    BusStopDao busStopDao;

    public PlaceParser() {
        busStopDao = new BusStopDao();
    }

    public String getPlaceSearchText(String address, boolean useLatLng) {
        if (BusStopParser.isEncodedBusStop(address)) {
            BusStop busStop = BusStopParser.decodeBusStop(busStopDao, address);

            if (useLatLng) {
                return busStop.getLatitude() + ", " + busStop.getLongitude();
            } else {
                return busStop.getStationId();
            }
        }
        return address;
    }

    public String prettyPrintPlace(String address) {
        if (BusStopParser.isEncodedBusStop(address)) {
            return BusStopParser.prettyPrintBusStop(busStopDao, address);
        }
        return address;
    }

    public String encodeBusStop(BusStop busStop) {
        return BusStopParser.encodeBusStop(busStop);
    }

}
