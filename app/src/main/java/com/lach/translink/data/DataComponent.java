package com.lach.translink.data;

import com.lach.translink.ui.history.HistoryDialog;
import com.lach.translink.ui.resolve.ResolveLocationActivity;
import com.lach.translink.ui.resolve.ResolveLocationListFragment;
import com.lach.translink.ui.search.SearchPlaceViewModel;
import com.lach.translink.ui.search.SearchViewModel;
import com.lach.translink.ui.search.dialog.FavouriteJourneysDialog;
import com.lach.translink.ui.search.dialog.SaveJourneyDialog;
import com.lach.translink.ui.search.dialog.SavedLocationsDialog;
import com.lach.translink.ui.settings.FavouriteJourneysActivity;
import com.lach.translink.ui.settings.FavouriteLocationsActivity;
import com.lach.translink.ui.settings.SettingsFragment;
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
}
