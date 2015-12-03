package com.lach.translink.ui.resolve;

import com.google.android.gms.maps.model.LatLng;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.data.place.bus.BusStop;

public interface ResolveLocationEvents {

    class ShowMapEvent {
    }

    class MapAddressSelectedEvent {
        private final LatLng point;

        public MapAddressSelectedEvent(LatLng point) {
            this.point = point;
        }

        public LatLng getPoint() {
            return point;
        }
    }

    class MapBusStopSelectedEvent {
        private final BusStop busStop;

        public MapBusStopSelectedEvent(BusStop busStop) {
            this.busStop = busStop;
        }

        public BusStop getBusStop() {
            return busStop;
        }
    }

    class LocationSelectedEvent {
        private final PlaceType placeType;
        private final String address;
        private final boolean favourite;

        public LocationSelectedEvent(PlaceType placeType, String address, boolean favourite) {
            this.placeType = placeType;
            this.address = address;
            this.favourite = favourite;
        }

        public PlaceType getPlaceType() {
            return placeType;
        }

        public String getAddress() {
            return address;
        }

        public boolean isFavourite() {
            return favourite;
        }
    }
}
