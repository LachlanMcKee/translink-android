package com.lach.common.data.map;

public interface MapMarker<T> {
    String getId();

    MapPosition getMapPosition();

    T getMarker();
}
