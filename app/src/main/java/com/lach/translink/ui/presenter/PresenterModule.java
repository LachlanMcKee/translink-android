package com.lach.translink.ui.presenter;

import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.location.LocationDataModule;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.tasks.place.TaskGetBusStops;
import com.lach.translink.tasks.resolve.TaskFindLocation;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenter;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.ui.presenter.resolve.ResolveLocationMapPresenter;
import com.lach.translink.ui.presenter.resolve.ResolveLocationMapPresenterImpl;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;

@Module(includes = {LocationDataModule.class})
public class PresenterModule {

    @Provides
    ResolveLocationListPresenter provideResolveLocationListPresenter(PreferencesProvider preferencesProvider, LocationHistoryDao locationHistoryDao,
                                                                     Provider<TaskFindLocation> taskFindLocationProvider, Provider<TaskGetAddress> getAddressesAsyncTaskProvider) {

        return new ResolveLocationListPresenterImpl(preferencesProvider, locationHistoryDao, taskFindLocationProvider, getAddressesAsyncTaskProvider);
    }

    @Provides
    ResolveLocationMapPresenter provideResolveLocationMapPresenter(PreferencesProvider preferencesProvider, Provider<TaskGetBusStops> taskGetBusStopsProvider) {

        return new ResolveLocationMapPresenterImpl(preferencesProvider, taskGetBusStopsProvider);
    }

}
