package com.lach.translink.ui.resolve;

import com.google.android.gms.maps.model.LatLng;
import com.lach.translink.data.location.PlaceType;

public interface ResolveLocationEvents {

    class ShowMapEvent {
    }

    class MapMarkerSelectedEvent {
        private final LatLng point;

        public MapMarkerSelectedEvent(LatLng point) {
            this.point = point;
        }

        public LatLng getPoint() {
            return point;
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
