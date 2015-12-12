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
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.util.DataResource;

public interface DataComponent {
    void inject(ResolveLocationActivity inject);

    void inject(ResolveLocationListFragment inject);

    void inject(ResolveLocationListPresenterImpl inject);

    void inject(FavouriteJourneysActivity inject);

    void inject(FavouriteLocationsActivity inject);

    void inject(FavouriteJourneysDialog inject);

    void inject(HistoryDialog inject);

    void inject(SavedLocationsDialog inject);

    void inject(SaveJourneyDialog inject);

    void inject(SearchViewModel inject);

    void inject(SearchPlaceViewModel inject);

    void inject(SettingsFragment inject);

    void inject(DataResource inject);

    void inject(SearchActivity inject);

    void inject(ResolveLocationMapFragment inject);

    void inject(BusStopUpdateFragment inject);
}
