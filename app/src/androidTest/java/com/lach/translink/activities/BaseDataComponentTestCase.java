package com.lach.translink.activities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.CallSuper;

import com.lach.common.data.ApplicationContext;
import com.lach.common.data.provider.ContactAddressExtractor;
import com.lach.common.data.provider.ProviderHelperModule;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.DaggerDataComponent;
import com.lach.translink.data.DataComponent;
import com.lach.translink.data.journey.JourneyDataModule;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.data.location.LocationDataModule;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.data.place.PlaceDataModule;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.data.place.bus.BusStopDao;
import com.lach.translink.tasks.resolve.TaskFindLocation;
import com.lach.translink.ui.presenter.PresenterModule;

import org.mockito.Mockito;

public abstract class BaseDataComponentTestCase<T extends Activity> extends BaseTestCase<T> {
    // ProviderHelperModule
    protected ContactAddressExtractor contactAddressExtractor;

    // PlaceDataModule
    protected BusStopDao busStopDao;
    protected PlaceParser placeParser;

    // JourneyDataModule
    protected JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;
    protected JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    // LocationDataModule
    protected LocationFavouriteDao locationFavouriteDao;
    protected LocationHistoryDao locationHistoryDao;
    protected TaskGetAddress taskGetAddress;
    protected TaskFindLocation taskFindLocation;

    public BaseDataComponentTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    @CallSuper
    @Override
    public void postInit() {
        contactAddressExtractor = Mockito.mock(ContactAddressExtractor.class);

        // ProviderHelperModule
        busStopDao = Mockito.mock(BusStopDao.class);
        placeParser = new PlaceParser(busStopDao);

        // PlaceDataModule
        journeyCriteriaFavouriteDao = Mockito.mock(JourneyCriteriaFavouriteDao.class);
        journeyCriteriaHistoryDao = Mockito.mock(JourneyCriteriaHistoryDao.class);

        // JourneyDataModule
        locationFavouriteDao = Mockito.mock(LocationFavouriteDao.class);
        locationHistoryDao = Mockito.mock(LocationHistoryDao.class);

        // LocationDataModule
        taskGetAddress = Mockito.spy(new TaskGetAddress(null));
        taskFindLocation = Mockito.spy(new TaskFindLocation());

        DataComponent locationDataComponent = DaggerDataComponent.builder()
                .locationDataModule(createLocationDataModule())
                .journeyDataModule(createJourneyDataModule())
                .placeDataModule(createPlaceDataModule())
                .coreModule(createCoreModule())
                .presenterModule(createPresenterModule())
                .providerHelperModule(createProviderHelperModule())
                .build();

        // Inject the data component into the app being tested.
        getApplication().setDataComponent(locationDataComponent);
    }

    public ProviderHelperModule createProviderHelperModule() {
        return new ProviderHelperModule() {
            @Override
            public ContactAddressExtractor provideContactAddressExtractor() {
                return contactAddressExtractor;
            }
        };
    }

    public PresenterModule createPresenterModule() {
        return new PresenterModule();
    }

    public PlaceDataModule createPlaceDataModule() {
        return new PlaceDataModule() {
            @Override
            public BusStopDao provideBusStopDao() {
                return busStopDao;
            }

            @Override
            public PlaceParser providePlaceParser(BusStopDao busStopDao1) {
                return placeParser;
            }
        };
    }

    public JourneyDataModule createJourneyDataModule() {
        return new JourneyDataModule() {
            @Override
            public JourneyCriteriaFavouriteDao provideJourneyCriteriaFavouriteDao() {
                return journeyCriteriaFavouriteDao;
            }

            @Override
            public JourneyCriteriaHistoryDao provideJourneyCriteriaHistoryDao() {
                return journeyCriteriaHistoryDao;
            }
        };
    }

    public LocationDataModule createLocationDataModule() {
        return new LocationDataModule() {
            @Override
            public LocationFavouriteDao provideLocationFavouriteDao() {
                return locationFavouriteDao;
            }

            @Override
            public LocationHistoryDao provideLocationHistoryDao() {
                return locationHistoryDao;
            }

            @Override
            public TaskGetAddress provideTaskGetAddress(@ApplicationContext Context context) {
                return taskGetAddress;
            }

            @Override
            public TaskFindLocation provideTaskFindLocation() {
                return taskFindLocation;
            }
        };
    }

}
