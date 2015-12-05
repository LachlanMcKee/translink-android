package com.lach.translink.activities.data;

import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.tasks.resolve.TaskFindLocation;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockLocationDataModule {

    @Singleton
    @Provides
    LocationFavouriteDao provideLocationFavouriteDao() {
        return Mockito.mock(LocationFavouriteDao.class);
    }


    @Singleton
    @Provides
    LocationHistoryDao provideLocationHistoryDao() {
        return Mockito.mock(LocationHistoryDao.class);
    }

    @Singleton
    @Provides
    TaskGetAddress provideTaskGetAddress() {
        return Mockito.spy(new TaskGetAddress(null));
    }

    @Singleton
    @Provides
    TaskFindLocation provideTaskFindLocation() {
        return Mockito.spy(new TaskFindLocation());
    }

}
