package com.lach.translink.data.place;

import com.lach.translink.data.place.bus.BusStopDao;

import dagger.Module;
import dagger.Provides;

@Module
public class PlaceDataModule {

    @Provides
    public BusStopDao provideBusStopDao() {
        return new BusStopDao();
    }

    @Provides
    public PlaceParser providePlaceParser(BusStopDao busStopDao) {
        return new PlaceParser(busStopDao);
    }

}
