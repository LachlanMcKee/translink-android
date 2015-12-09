package com.lach.translink.data;

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
import com.lach.translink.util.DataResource;

public interface DataComponent {
    void inject(ResolveLocationActivity inject);

    void inject(ResolveLocationListFragment inject);

    void inject(FavouriteJourneysActivity inject);

    void inject(FavouriteLocationsActivity inject);

    void inject(FavouriteJourneysDialog favouriteJourneysDialog);

    void inject(HistoryDialog historyDialog);

    void inject(SavedLocationsDialog savedLocationsDialog);

    void inject(SaveJourneyDialog saveJourneyDialog);

    void inject(SearchViewModel searchViewModel);

    void inject(SearchPlaceViewModel searchPlaceViewModel);

    void inject(SettingsFragment settingsFragment);

    void inject(DataResource dataResource);

    void inject(SearchActivity searchActivity);

    void inject(ResolveLocationMapFragment resolveLocationMapFragment);

    void inject(BusStopUpdateFragment busStopUpdateFragment);
}
