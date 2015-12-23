package com.lach.common.data.map;

public final class MapBounds {
    public final MapPosition southWest;
    public final MapPosition northEast;

    public MapBounds(MapPosition southWest, MapPosition northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }
}
