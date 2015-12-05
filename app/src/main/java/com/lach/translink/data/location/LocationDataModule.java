package com.lach.translink.data.location;

import android.content.Context;

import com.lach.common.data.ApplicationContext;
import com.lach.common.data.CoreModule;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.data.place.PlaceDataModule;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.tasks.resolve.TaskFindLocation;

import dagger.Module;
import dagger.Provides;

@Module(includes = {CoreModule.class, PlaceDataModule.class})
public class LocationDataModule {

    @Provides
    LocationFavouriteDao provideLocationFavouriteDao() {
        return new LocationFavouriteDao();
    }

    @Provides
    LocationHistoryDao provideLocationHistoryDao(PlaceParser placeParser) {
        return new LocationHistoryDao(placeParser);
    }

    @Provides
    TaskGetAddress provideTaskGetAddress(@ApplicationContext Context context) {
        return new TaskGetAddress(context);
    }

    @Provides
    TaskFindLocation provideTaskFindLocation() {
        return new TaskFindLocation();
    }

}
