package com.lach.translink.data;

import com.lach.common.data.provider.ProviderHelperModule;
import com.lach.translink.data.journey.JourneyDataModule;
import com.lach.translink.data.location.LocationDataModule;
import com.lach.translink.data.place.PlaceDataModule;
import com.lach.translink.ui.impl.history.HistoryDialog;
import com.lach.translink.ui.impl.place.BusStopUpdateFragment;
import com.lach.translink.ui.impl.resolve.ResolveLocationActivity;
import com.lach.translink.ui.impl.resolve.ResolveLocationListFragment;
import com.lach.translink.ui.impl.resolve.ResolveLocationMapFragment;
import com.lach.translink.ui.impl.search.SearchActivity;
import com.lach.translink.ui.impl.search.SearchPlaceViewModel;
import com.lach.translink.ui.impl.search.SearchViewModel;
import com.lach.translink.ui.impl.search.dialog.FavouriteJourneysDialog;
import com.lach.translink.ui.impl.search.dialog.SaveJourneyDialog;
import com.lach.translink.ui.impl.search.dialog.SavedLocationsDialog;
import com.lach.translink.ui.impl.settings.FavouriteJourneysActivity;
import com.lach.translink.ui.impl.settings.FavouriteLocationsActivity;
import com.lach.translink.ui.impl.settings.SettingsFragment;
import com.lach.translink.ui.presenter.PresenterModule;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.util.DataResource;

import dagger.Component;

@Component(modules = {
        PresenterModule.class,
        LocationDataModule.class,
        JourneyDataModule.class,
        PlaceDataModule.class,
        ProviderHelperModule.class
})
public interface DataComponent {
    // Search screens.
    void inject(SearchActivity inject);

    void inject(SearchViewModel inject);

    void inject(SearchPlaceViewModel inject);

    // Resolve location screens.
    void inject(ResolveLocationActivity inject);

    void inject(ResolveLocationListFragment inject);

    void inject(ResolveLocationListPresenterImpl inject);

    void inject(ResolveLocationMapFragment inject);

    // Settings screens
    void inject(SettingsFragment inject);

    void inject(FavouriteJourneysActivity inject);

    void inject(FavouriteLocationsActivity inject);

    // Dialog screens.
    void inject(FavouriteJourneysDialog inject);

    void inject(HistoryDialog inject);

    void inject(SavedLocationsDialog inject);

    void inject(SaveJourneyDialog inject);

    // Misc screens.
    void inject(BusStopUpdateFragment inject);

    void inject(DataResource inject);
}
