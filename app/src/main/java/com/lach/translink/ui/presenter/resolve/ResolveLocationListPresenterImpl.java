package com.lach.translink.ui.presenter.resolve;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.lach.common.async.AsyncResult;
import com.lach.common.data.map.MapPosition;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.log.Log;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.data.location.history.LocationHistory;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.tasks.resolve.TaskFindLocation;
import com.lach.translink.ui.impl.UiPreference;
import com.lach.translink.ui.impl.resolve.ResolveLocationEvents;
import com.lach.translink.ui.view.resolve.ResolveLocationListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import de.greenrobot.event.EventBus;

public class ResolveLocationListPresenterImpl implements ResolveLocationListPresenter {
    private static final String TAG = ResolveLocationListPresenterImpl.class.getSimpleName();

    private static final int TASK_GET_ADDRESS = 1;
    private static final int TASK_SEARCH_TRANSLINK = 2;

    private static final String BUNDLE_SEARCH_RESULTS = "search_results";
    private static final String BUNDLE_HISTORY = "history";

    private static final String BUNDLE_TRANSLINK_SEARCH_TERM = "translink_search";
    private static final String BUNDLE_COORDINATES_SEARCH_LATITUDE = "coordinates_lat";
    private static final String BUNDLE_COORDINATES_SEARCH_LONGITUDE = "coordinates_long";

    private static final String BUNDLE_SEARCH_TEXT = "search_text";
    private static final String BUNDLE_PREVIOUS_SEARCH_TEXT = "previous_search_text";

    private static final String BUNDLE_CURRENT_UI_MODE = "current_ui_mode";

    // Search must contain at least three characters, or three numbers without spaces.
    private static final Pattern PATTERN_VALID_ADDRESS_SEARCH = Pattern.compile("[a-zA-Z]{3,}");

    // Search values.
    private String previousSearchText;
    private String currentTranslinkLookupText;
    private MapPosition addressLookupPosition;

    private ResolveLocationListPresenterImpl.UiMode mCurrentUIMode;

    private ResolveLocationListView mView;
    private PlaceType placeType;

    private ArrayList<String> mSearchResults;
    private ArrayList<LocationHistory> mLocationHistoryList;

    private TranslinkSearchHandler mTranslinkSearchHandler;

    private final PlaceParser placeParser;
    private final PreferencesProvider preferencesProvider;
    private final LocationHistoryDao locationHistoryDao;

    private final Provider<TaskFindLocation> taskFindLocationProvider;
    private final Provider<TaskGetAddress> getAddressesAsyncTaskProvider;

    @Inject
    public ResolveLocationListPresenterImpl(PlaceParser placeParser, PreferencesProvider preferencesProvider, LocationHistoryDao locationHistoryDao,
                                            Provider<TaskFindLocation> taskFindLocationProvider, Provider<TaskGetAddress> getAddressesAsyncTaskProvider) {

        this.placeParser = placeParser;
        this.preferencesProvider = preferencesProvider;
        this.locationHistoryDao = locationHistoryDao;
        this.taskFindLocationProvider = taskFindLocationProvider;
        this.getAddressesAsyncTaskProvider = getAddressesAsyncTaskProvider;
    }

    @Override
    public void onCreate(ResolveLocationListView view, Bundle savedInstanceState) {
        mView = view;

        mTranslinkSearchHandler = new TranslinkSearchHandler(this);

        Preferences preferences = preferencesProvider.getPreferences();
        view.toggleKeyboard(UiPreference.AUTOMATIC_KEYBOARD.get(preferences));

        boolean historyReloaded = false;
        if (savedInstanceState != null) {
            ArrayList<LocationHistory> savedHistory = savedInstanceState.getParcelableArrayList(BUNDLE_HISTORY);

            if (savedHistory != null) {
                historyReloaded = true;
                onHistoryUpdated(savedHistory);
            }
        }

        if (!historyReloaded) {
            Log.debug(TAG, "onViewCreated. Loading history");

            locationHistoryDao.setHistoryLoadListener(new LocationHistoryDao.HistoryLoadListener() {
                @Override
                public void onHistoryLoaded(List<? extends LocationHistory> locationHistoryList) {
                    locationHistoryDao.removeHistoryLoadListener();

                    boolean isUiReady = mView.isUiReady();
                    Log.debug(TAG, "onHistoryLoaded. isUiReady: " + isUiReady);

                    // UI may have been destroyed before the history is loaded.
                    if (!isUiReady) {
                        Log.warn(TAG, "onHistoryLoaded. ui not ready.");
                        return;
                    }

                    onHistoryUpdated(new ArrayList<>(locationHistoryList));
                }
            });
            locationHistoryDao.loadHistory();
        }

        // An address lookup request may have been sent during a configuration change.
        boolean outstandingRequest = (addressLookupPosition != null);
        if (outstandingRequest) {
            updateLookupAddressInternal(addressLookupPosition);

        } else {
            // If the address lookup wasn't provided, attempt to get it from the saved state.
            ArrayList<String> existingAddressList = null;
            mCurrentUIMode = UiMode.NORMAL;

            if (savedInstanceState != null) {
                mCurrentUIMode = (UiMode) savedInstanceState.getSerializable(BUNDLE_CURRENT_UI_MODE);

                double addressLookupLatitude = savedInstanceState.getDouble(BUNDLE_COORDINATES_SEARCH_LATITUDE, -1);
                double addressLookupLongitude = savedInstanceState.getDouble(BUNDLE_COORDINATES_SEARCH_LONGITUDE, -1);
                if (addressLookupLatitude != -1 && addressLookupLongitude != -1) {
                    addressLookupPosition = new MapPosition(addressLookupLatitude, addressLookupLongitude);
                }

                currentTranslinkLookupText = savedInstanceState.getString(BUNDLE_TRANSLINK_SEARCH_TERM);

                // Ensure that the UI mode is not invalid. Perhaps a thread was cancelled, and the UI mode was not properly reset.
                if (mCurrentUIMode == null ||
                        (mCurrentUIMode == UiMode.ADDRESS_LOOKUP && addressLookupPosition == null) ||
                        (mCurrentUIMode == UiMode.TRANSLINK_LOOKUP && currentTranslinkLookupText == null)) {

                    mCurrentUIMode = UiMode.NORMAL;
                }

                previousSearchText = savedInstanceState.getString(BUNDLE_PREVIOUS_SEARCH_TEXT);
                view.updateSearchText(savedInstanceState.getString(BUNDLE_SEARCH_TEXT));

                existingAddressList = savedInstanceState.getStringArrayList(BUNDLE_SEARCH_RESULTS);
            }

            updateUi(mCurrentUIMode, existingAddressList);

            // Only add the watcher if a lookup address isn't supplied.
            view.toggleSearchListener(true);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTranslinkSearchHandler != null) {
            mTranslinkSearchHandler.removeCallbacksAndMessages(null);
        }

        outState.putStringArrayList(BUNDLE_SEARCH_RESULTS, mSearchResults);
        outState.putParcelableArrayList(BUNDLE_HISTORY, mLocationHistoryList);

        // This is important for saving the current searching state.
        if (currentTranslinkLookupText != null) {
            outState.putString(BUNDLE_TRANSLINK_SEARCH_TERM, currentTranslinkLookupText);
        }
        if (addressLookupPosition != null) {
            outState.putDouble(BUNDLE_COORDINATES_SEARCH_LATITUDE, addressLookupPosition.getLatitude());
            outState.putDouble(BUNDLE_COORDINATES_SEARCH_LONGITUDE, addressLookupPosition.getLongitude());
        }
        outState.putString(BUNDLE_PREVIOUS_SEARCH_TEXT, previousSearchText);

        outState.putString(BUNDLE_SEARCH_TEXT, mView.getSearchText());
        outState.putSerializable(BUNDLE_CURRENT_UI_MODE, mCurrentUIMode);
    }

    private void onHistoryUpdated(ArrayList<LocationHistory> locationHistory) {
        mLocationHistoryList = locationHistory;

        ArrayList<ResolveLocationListView.LocationInfo> locationInfoList = new ArrayList<>();
        for (LocationHistory history : locationHistory) {
            String address = history.getAddress();
            locationInfoList.add(new ResolveLocationListView.LocationInfo(address, placeParser.prettyPrintPlace(address)));
        }

        mView.updateHistory(locationInfoList);
    }

    @Override
    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    @Override
    public void updateSearch(SearchType searchType) {
        Log.debug(TAG, "updateSearch");

        // The handler is only doing one thing, don't bother about the 'what'
        mTranslinkSearchHandler.removeCallbacksAndMessages(null);

        executeNewSearch(searchType);
    }

    @Override
    public void executeSearch() {
        if (!mView.isUiVisible()) {
            Log.warn(TAG, "executeSearch. ui not visible");
            return;
        }

        if (mCurrentUIMode != UiMode.TRANSLINK_LOOKUP) {
            Log.debug(TAG, "executeSearch. UI mode is no longer valid.");
            return;
        }

        currentTranslinkLookupText = mView.getSearchText();
        Log.debug(TAG, "executeSearch. Text: " + currentTranslinkLookupText);

        mView.createTask(TASK_SEARCH_TRANSLINK, taskFindLocationProvider.get())
                .parameters(TaskFindLocation.createParams(currentTranslinkLookupText))
                .executeImmediately(true)
                .start();
    }

    @Override
    public void executeNewSearch(SearchType searchType) {
        Log.debug(TAG, "- executeNewSearch. searchType: " + searchType);

        // Cancel any outstanding search tasks.
        mView.cancelCurrentTask(true);

        // This may be fired while transitioning to the map ui.
        if (!mView.isUiVisible()) {
            Log.warn(TAG, "executeNewSearch. ui not visible");
            return;
        }

        UiMode newUIMode = UiMode.NORMAL;
        if (searchType == SearchType.TRANSLINK) {
            String searchText = mView.getSearchText();

            // Check that the input is valid.
            boolean isValid = searchText.length() >= 3;
            if (isValid) {
                // Special case for stop ids. It must be at least three numbers, and at most six.
                isValid = searchText.matches("[0-9]{3,6}");

                if (!isValid) {
                    // Check if there are at least 3 alphabet characters in a row before starting the search.
                    isValid = PATTERN_VALID_ADDRESS_SEARCH.matcher(searchText).find();
                }
            }

            if (isValid) {
                newUIMode = UiMode.TRANSLINK_LOOKUP;
            }

        } else {
            // Clear the text from the search textbox.
            updateSearchText("", false);

            if (searchType == SearchType.COORDINATES && addressLookupPosition != null) {
                newUIMode = UiMode.ADDRESS_LOOKUP;
            }
        }

        updateUi(newUIMode, null);

        // Execute the respective search.
        switch (newUIMode) {
            case ADDRESS_LOOKUP:
                mView.createTask(TASK_GET_ADDRESS, getAddressesAsyncTaskProvider.get())
                        .parameters(new LatLng(addressLookupPosition.getLatitude(), addressLookupPosition.getLongitude()))
                        .executeImmediately(true)
                        .start();
                break;

            case TRANSLINK_LOOKUP:
                mTranslinkSearchHandler.sendMessageDelayed(Message.obtain(mTranslinkSearchHandler), 300);
                break;
        }
    }

    @Override
    public void retrySearch() {
        switch (mCurrentUIMode) {
            case ADDRESS_ERROR:
                executeNewSearch(SearchType.COORDINATES);
                break;

            case TRANSLINK_ERROR:
                executeNewSearch(SearchType.TRANSLINK);
                break;
        }
    }

    private void updateUi(UiMode uiMode, ArrayList<String> existingAddressList) {
        mCurrentUIMode = uiMode;

        // Clear the adapter data since no search is occurring.
        updateSearchResults(null);

        mView.updateUi(mCurrentUIMode);

        if (uiMode.searchType == ResolveLocationListPresenterImpl.SearchType.COORDINATES) {
            mView.updateSearchMode(false, addressLookupPosition.latitude + ", " + addressLookupPosition.longitude);

        } else {
            mView.updateSearchMode(true, getSearchHint());
        }

        if (uiMode == UiMode.SHOW_RESULTS) {
            updateSearchResults(existingAddressList);
        }
    }

    private String getSearchHint() {
        String description;
        if (placeType != null) {
            description = placeType.getDescription().toLowerCase();
        } else {
            description = "location";
        }
        return "Choose " + description;
    }

    @Override
    public void onSearchTextChanged(String searchText) {
        if (previousSearchText != null) {
            if (searchText.trim().toLowerCase().equals(previousSearchText.trim().toLowerCase())) {
                return;
            }
        }
        previousSearchText = searchText;

        Log.debug(TAG, "search text change triggered with unique text.");
        updateSearch(SearchType.TRANSLINK);
    }

    @Override
    public void updateStreet(String street) {
        Log.debug(TAG, "updateStreet: " + street);
        if (street.contains("-")) {
            street = street.substring(street.indexOf("-") + 1);
        }

        updateSearchText(street, true);
    }

    private void updateSearchText(String newText, boolean executeSearch) {
        mView.toggleSearchListener(false);
        mView.updateSearchText(newText);
        mView.toggleSearchListener(true);

        if (executeSearch) {
            updateSearch(SearchType.TRANSLINK);
        }
    }

    @Override
    public void setMapLookupPoint(MapPosition point) {
        addressLookupPosition = point;

        // If the UI hasn't been inflated yet, this will be updated later.
        if (!mView.isUiReady()) {
            return;
        }
        updateLookupAddressInternal(addressLookupPosition);
    }

    private void updateLookupAddressInternal(MapPosition point) {
        Log.debug(TAG, "updateLookupAddressInternal. point: " + point);

        addressLookupPosition = point;
        if (addressLookupPosition != null) {
            updateSearch(SearchType.COORDINATES);
        }
    }

    private void updateSearchResults(ArrayList<String> addressList) {
        int size = (addressList != null ? addressList.size() : 0);
        Log.debug(TAG, "updateSearchResults. count: " + size);

        mSearchResults = addressList;

        if (size == 0) {
            mView.updateSearchResults(null);
            return;
        }

        List<ResolveLocationListView.LocationInfo> locationInfoList = new ArrayList<>();
        for (String address : addressList) {
            locationInfoList.add(new ResolveLocationListView.LocationInfo(address, address));
        }
        mView.updateSearchResults(locationInfoList);
    }

    @Override
    public void sendSaveLocationSelectedEvent(String address) {
        if (address != null) {
            EventBus.getDefault().post(new ResolveLocationEvents.LocationSelectedEvent(address));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        if (!mView.isUiVisible()) {
            Log.warn(TAG, "onTaskFinished. ui not visible");
            return;
        }

        switch (taskId) {
            case TASK_GET_ADDRESS:
                List<String> addresses = (List<String>) result.getItem();
                setMapLookupPoint(null);

                if (addresses.size() > 0) {
                    updateStreet(addresses.get(0));
                } else {
                    mView.showNotification("No results found.");
                }
                break;

            case TASK_SEARCH_TRANSLINK:
                ArrayList<String> translinkAddressList = (ArrayList<String>) result.getItem();
                currentTranslinkLookupText = null;
                updateUi(UiMode.SHOW_RESULTS, translinkAddressList);
                break;
        }
    }

    @Override
    public void onTaskCancelled(int taskId) {
        Log.debug(TAG, "onTaskCancelled: " + taskId);

        if (!mView.isUiVisible()) {
            Log.warn(TAG, "onTaskCancelled. ui not visible");
            return;
        }

        switch (taskId) {
            case TASK_GET_ADDRESS:
                updateLookupAddressInternal(null);
                break;

            case TASK_SEARCH_TRANSLINK:
                currentTranslinkLookupText = null;
                break;
        }
    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        if (!mView.isUiVisible()) {
            Log.warn(TAG, "onTaskError. ui not visible");
            return true;
        }

        if (taskId == TASK_SEARCH_TRANSLINK) {
            updateUi(ResolveLocationListPresenterImpl.UiMode.TRANSLINK_ERROR, null);

        } else if (taskId == TASK_GET_ADDRESS) {
            updateUi(UiMode.ADDRESS_ERROR, null);
        }

        return true;
    }

    static class TranslinkSearchHandler extends Handler {
        final WeakReference<ResolveLocationListPresenterImpl> mPresenterRef;

        TranslinkSearchHandler(ResolveLocationListPresenterImpl presenter) {
            super();
            mPresenterRef = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message message) {
            ResolveLocationListPresenterImpl presenter = mPresenterRef.get();
            if (presenter == null) {
                Log.warn(TAG, "TranslinkSearchHandler. presenter is gone");
                return;
            }
            presenter.executeSearch();
        }
    }

}
