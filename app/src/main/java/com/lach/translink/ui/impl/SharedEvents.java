package com.lach.translink.ui.impl;

import com.lach.translink.data.location.PlaceType;

public interface SharedEvents {
    class SavedLocationSelectedEvent {

        private final String address;
        private final PlaceType placeType;

        public SavedLocationSelectedEvent(String address, PlaceType placeType) {
            this.address = address;
            this.placeType = placeType;
        }

        public String getAddress() {
            return address;
        }

        public PlaceType getPlaceType() {
            return placeType;
        }
    }
}
