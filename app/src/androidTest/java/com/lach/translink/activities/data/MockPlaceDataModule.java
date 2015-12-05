package com.lach.translink.activities.data;

import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.data.place.bus.BusStopDao;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockPlaceDataModule {

    @Singleton
    @Provides
    BusStopDao provideBusStopDao() {
        return Mockito.mock(BusStopDao.class);
    }

    @Singleton
    @Provides
    PlaceParser providePlaceParser(BusStopDao busStopDao) {
        return new PlaceParser(busStopDao);
    }

}
