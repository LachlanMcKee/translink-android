package com.lach.translink.ui.presenter.resolve;

import com.lach.common.data.map.MapPosition;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.ui.presenter.TaskPresenter;
import com.lach.translink.ui.view.resolve.ResolveLocationListView;

public interface ResolveLocationListPresenter extends TaskPresenter<ResolveLocationListView> {
    void setPlaceType(PlaceType placeType);

    void updateSearch(SearchType searchType);

    void executeSearch();

    void executeNewSearch(SearchType searchType);

    void retrySearch();

    void onSearchTextChanged(String searchText);

    void updateStreet(String street);

    void setMapLookupPoint(MapPosition point);

    void sendSaveLocationSelectedEvent(String address);

    enum UiMode {
        // Regular state.
        NORMAL,
        // Lookup states.
        ADDRESS_LOOKUP(SearchType.COORDINATES, true), TRANSLINK_LOOKUP(SearchType.TRANSLINK, true),
        // Lookup error states.
        ADDRESS_ERROR(SearchType.COORDINATES, true), TRANSLINK_ERROR(SearchType.TRANSLINK, true),
        // Results list state.
        SHOW_RESULTS;

        public final SearchType searchType;
        public final boolean isSearchMode;

        UiMode() {
            this(null);
        }

        UiMode(SearchType searchType) {
            this(searchType, false);
        }

        UiMode(SearchType searchType, boolean isSearchMode) {
            this.searchType = searchType;
            this.isSearchMode = isSearchMode;
        }
    }

    enum SearchType {
        COORDINATES, TRANSLINK, CLEAR_SEARCH
    }
}
