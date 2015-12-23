package com.lach.translink.data.location;

import android.content.Context;

import com.lach.common.data.ApplicationContext;
import com.lach.common.data.CoreModule;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.network.NetworkModule;
import com.lach.translink.tasks.resolve.TaskFindLocation;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;

@Module(includes = {NetworkModule.class, CoreModule.class})
public class LocationDataModule {

    @Provides
    public LocationFavouriteDao provideLocationFavouriteDao() {
        return new LocationFavouriteDao();
    }

    @Provides
    public LocationHistoryDao provideLocationHistoryDao() {
        return new LocationHistoryDao();
    }

    @Provides
    public TaskGetAddress provideTaskGetAddress(@ApplicationContext Context context) {
        return new TaskGetAddress(context);
    }

    @Provides
    public TaskFindLocation provideTaskFindLocation(OkHttpClient okHttpClient) {
        return new TaskFindLocation(okHttpClient);
    }

}
