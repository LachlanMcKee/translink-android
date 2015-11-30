package com.lach.translink.ui.resolve;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.data.provider.ContactAddressExtractor;
import com.lach.common.tasks.TaskGetAddress;
import com.lach.common.log.Log;
import com.lach.common.util.NetworkUtil;
import com.lach.common.ui.widget.WrapLinearLayoutManager;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.tasks.resolve.TaskFindLocation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveLocationListFragment extends AsyncTaskFragment {
    private static final String TAG = "ResolveLocationListFragment";

    private static final int TASK_GET_ADDRESS = 1;
    private static final int TASK_SEARCH_TRANSLINK = 2;

    private static final int CONTACTS_REQUEST = 1;
    private static final int MICROPHONE_REQUEST = 2;

    private static final String PLACE_TYPE = "place_type";

    private static final String BUNDLE_SEARCH_RESULTS = "search_results";
    private static final String BUNDLE_HISTORY = "history";

    private static final String BUNDLE_TRANSLINK_SEARCH_TERM = "translink_search";
    private static final String BUNDLE_COORDINATES_SEARCH_TERM = "coordinates";

    private static final String BUNDLE_SEARCH_TEXT = "search_text";
    private static final String BUNDLE_PREVIOUS_SEARCH_TEXT = "previous_search_text";

    private static final String BUNDLE_CURRENT_UI_MODE = "current_ui_mode";

    @InjectView(R.id.resolve_list_root)
    View mRoot;

    @InjectView(R.id.resolve_list_scroll_view)
    ScrollView mScrollView;

    @InjectView(R.id.resolve_list_separator)
    View mScrollSeparator;

    @InjectView(R.id.resolve_location_list_text)
    TextView mLocationText;

    @InjectView(R.id.resolve_list_microphone)
    View mMicrophoneButton;

    @InjectView(R.id.resolve_list_clear)
    View mClearButton;

    @InjectView(R.id.resolve_choose_on_map)
    View mChooseMapButton;

    @InjectView(R.id.resolve_open_contacts)
    View mOpenContactsButton;

    @InjectView(R.id.resolve_history_container)
    View mHistoryContainer;

    @InjectView(R.id.resolve_history_list)
    RecyclerView mHistoryList;

    @InjectView(R.id.resolve_results_container)
    View mResultsContainer;

    @InjectView(R.id.resolve_searching_container)
    View mSearchingContainer;

    @InjectView(R.id.resolve_searching_label)
    TextView mSearchingLabel;

    @InjectView(R.id.resolve_error_container)
    View mErrorContainer;

    @InjectView(R.id.resolve_error_label)
    TextView mErrorLabel;

    @InjectView(R.id.resolve_results_list)
    RecyclerView mResultsList;

    // Search values.
    private String previousSearchText;
    private String currentTranslinkLookupText;
    private LatLng addressLookupLatLng;

    private UiMode mCurrentUIMode;

    private TranslinkSearchHandler translinkSearchHandler;

    private ResultAdapter mResultAdapter;
    private HistoryAdapter mHistoryAdapter;
    private PlaceType placeType;

    private boolean isVoiceRecognitionAvailable;

    private TextWatcher mSearchTextWatcher;

    @Inject
    ContactAddressExtractor contactAddressExtractor;

    @Inject
    LocationHistoryDao locationHistoryDao;

    @Inject
    Provider<TaskFindLocation> taskFindLocationProvider;

    @Inject
    Provider<TaskGetAddress> getAddressesAsyncTaskProvider;

    public static ResolveLocationListFragment newInstance(PlaceType placeType) {
        ResolveLocationListFragment f = new ResolveLocationListFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable(PLACE_TYPE, placeType);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.debug(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        translinkSearchHandler = new TranslinkSearchHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.debug(TAG, "onCreateView");
        placeType = (PlaceType) getArguments().getSerializable(PLACE_TYPE);

        return inflater.inflate(R.layout.f_resolve_location_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.debug(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = getActivity();
        TranslinkApplication application = (TranslinkApplication) activity.getApplication();
        application.getDataComponent().inject(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (preferences.getBoolean("AutomaticKeyboard", false)) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        } else {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        // Init the results list.
        final WrapLinearLayoutManager resultsLayoutManager = new WrapLinearLayoutManager(activity);
        resultsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mResultsList.setLayoutManager(resultsLayoutManager);

        mResultAdapter = new ResultAdapter(new LocationViewHolder.LocationClickListener() {
            @Override
            public void onResultClicked(int position) {
                sendSaveLocationSelectedEvent(mResultAdapter.getAddressList().get(position));
            }
        });

        mResultsList.setAdapter(mResultAdapter);

        // Init the history list.
        final WrapLinearLayoutManager historyLayoutManager = new WrapLinearLayoutManager(activity);
        historyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mHistoryList.setLayoutManager(historyLayoutManager);

        mHistoryAdapter = new HistoryAdapter(new LocationViewHolder.LocationClickListener() {
            @Override
            public void onResultClicked(int position) {
                sendSaveLocationSelectedEvent(mHistoryAdapter.getAddressList().get(position));
            }
        });

        boolean historyReloaded = false;
        if (savedInstanceState != null) {
            ArrayList<String> savedHistory = savedInstanceState.getStringArrayList(BUNDLE_HISTORY);

            if (savedHistory != null) {
                historyReloaded = true;
                mHistoryAdapter.changeData(savedHistory);
                changeHistoryVisibility(View.VISIBLE);
            }
        }

        mHistoryList.setAdapter(mHistoryAdapter);

        if (!historyReloaded) {
            Log.debug(TAG, "onViewCreated. Loading history");

            locationHistoryDao.setHistoryLoadListener(new LocationHistoryDao.HistoryLoadListener() {
                @Override
                public void onHistoryLoaded(ArrayList<String> locationHistoryList) {
                    locationHistoryDao.removeHistoryLoadListener();

                    boolean isAdded = isAdded();
                    Log.debug(TAG, "onHistoryLoaded. isAdded: " + isAdded + ", isVisible:" + isVisible());

                    // Fragment may be destroyed before the history is loaded.
                    if (!isAdded || mHistoryAdapter == null) {
                        Log.warn(TAG, "onHistoryLoaded. fragment not added. mHistoryAdapter: " + mHistoryAdapter);
                        return;
                    }

                    mHistoryAdapter.changeData(locationHistoryList);
                    changeHistoryVisibility(View.VISIBLE);
                }
            });
            locationHistoryDao.loadHistory();
        }

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                // Apparently the scroll view might not be accessible when this fires.
                if (mScrollView == null) {
                    return;
                }

                int scrollY = mScrollView.getScrollY();

                if (scrollY > 0 && mScrollSeparator.getVisibility() == View.INVISIBLE) {
                    mScrollSeparator.setVisibility(View.VISIBLE);

                } else if (scrollY == 0 && mScrollSeparator.getVisibility() == View.VISIBLE) {
                    mScrollSeparator.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Check to see if a recognition activity is present
        PackageManager pm = activity.getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        isVoiceRecognitionAvailable = (activities.size() > 0);

        if (!isVoiceRecognitionAvailable) {
            mMicrophoneButton.setVisibility(View.GONE);
        }

        // Don't restore the search text automatically, this breaks the map integration.
        mLocationText.setSaveEnabled(false);

        // Add the text listener right at the end to prevent unneeded callbacks.
        mSearchTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                String newTextValue = text.toString();
                if (previousSearchText != null) {
                    if (newTextValue.trim().toLowerCase().equals(previousSearchText.trim().toLowerCase())) {
                        return;
                    }
                }
                previousSearchText = newTextValue;

                Log.debug(TAG, "search text change triggered with unique text.");
                updateSearch(SearchType.TRANSLINK);
            }
        };

        // The parent activity may have sent an address lookup request while the fragment was not created yet.
        boolean outstandingRequest = (addressLookupLatLng != null);
        if (outstandingRequest) {
            updateLookupAddressInternal(addressLookupLatLng);

        } else {
            // If the address lookup wasn't provided, attempt to get it from the saved state.
            ArrayList<String> existingAddressList = null;
            mCurrentUIMode = UiMode.NORMAL;

            if (savedInstanceState != null) {
                mCurrentUIMode = (UiMode) savedInstanceState.getSerializable(BUNDLE_CURRENT_UI_MODE);
                addressLookupLatLng = savedInstanceState.getParcelable(BUNDLE_COORDINATES_SEARCH_TERM);
                currentTranslinkLookupText = savedInstanceState.getString(BUNDLE_TRANSLINK_SEARCH_TERM);

                // Ensure that the UI mode is not invalid. Perhaps a thread was cancelled, and the UI mode was not properly reset.
                if (mCurrentUIMode == null ||
                        (mCurrentUIMode == UiMode.ADDRESS_LOOKUP && addressLookupLatLng == null) ||
                        (mCurrentUIMode == UiMode.TRANSLINK_LOOKUP && currentTranslinkLookupText == null)) {

                    mCurrentUIMode = UiMode.NORMAL;
                }

                previousSearchText = savedInstanceState.getString(BUNDLE_PREVIOUS_SEARCH_TEXT);
                mLocationText.setText(savedInstanceState.getString(BUNDLE_SEARCH_TEXT));

                existingAddressList = savedInstanceState.getStringArrayList(BUNDLE_SEARCH_RESULTS);
            }

            updateUi(mCurrentUIMode, existingAddressList);

            // Only add the watcher if a lookup address isn't supplied.
            mLocationText.addTextChangedListener(mSearchTextWatcher);
        }
    }

    @Override
    public void onResume() {
        Log.debug(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.debug(TAG, "onSaveInstanceState");

        if (translinkSearchHandler != null) {
            translinkSearchHandler.removeCallbacksAndMessages(null);
        }

        if (mResultAdapter != null) {
            outState.putStringArrayList(BUNDLE_SEARCH_RESULTS, mResultAdapter.getAddressList());
        }

        if (mHistoryAdapter != null) {
            outState.putStringArrayList(BUNDLE_HISTORY, mHistoryAdapter.getAddressList());
        }

        // This is important for saving the current searching state.
        if (currentTranslinkLookupText != null) {
            outState.putString(BUNDLE_TRANSLINK_SEARCH_TERM, currentTranslinkLookupText);
        }
        if (addressLookupLatLng != null) {
            outState.putParcelable(BUNDLE_COORDINATES_SEARCH_TERM, addressLookupLatLng);
        }
        outState.putString(BUNDLE_PREVIOUS_SEARCH_TEXT, previousSearchText);

        if (mLocationText != null) {
            outState.putString(BUNDLE_SEARCH_TEXT, mLocationText.getText().toString());
        }
        outState.putSerializable(BUNDLE_CURRENT_UI_MODE, mCurrentUIMode);

        super.onSaveInstanceState(outState);
    }

    private String getAddressText(Address address) {
        String line1 = address.getAddressLine(0);
        String line2 = address.getLocality();
        if (line2 != null) {
            return line1 + ", " + line2;
        }
        return line1;
    }

    @OnClick(R.id.resolve_list_back_button)
    void goBack() {
        // Ensure no outstanding tasks are running.
        cancelCurrentTask(true);

        getActivity().finish();
    }

    @OnClick(R.id.resolve_list_microphone)
    void openMicrophoneDialog() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-AU");
        try {
            startActivityForResult(i, MICROPHONE_REQUEST);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    void updateSearch(SearchType searchType) {
        Log.debug(TAG, "updateSearch");

        // The handler is only doing one thing, don't bother about the 'what'
        translinkSearchHandler.removeCallbacksAndMessages(null);

        executeNewSearch(searchType);
    }

    public boolean handleBackPressed() {
        if (mLocationText.length() > 0) {
            clearSearch();
            return true;
        }
        goBack();
        return true;
    }

    static class TranslinkSearchHandler extends Handler {
        final WeakReference<ResolveLocationListFragment> mFragmentRef;

        TranslinkSearchHandler(ResolveLocationListFragment aFragment) {
            super();
            mFragmentRef = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message message) {
            ResolveLocationListFragment fragment = mFragmentRef.get();
            if (fragment == null) {
                Log.warn(TAG, "TranslinkSearchHandler. Fragment is gone");
                return;
            }

            fragment.executeSearch();
        }
    }

    void executeSearch() {
        if (!isVisible()) {
            Log.warn(TAG, "executeSearch. fragment not visible");
            return;
        }

        if (mCurrentUIMode != UiMode.TRANSLINK_LOOKUP) {
            Log.debug(TAG, "executeSearch. UI mode is no longer valid.");
            return;
        }

        currentTranslinkLookupText = mLocationText.getText().toString();
        Log.debug(TAG, "executeSearch. Text: " + currentTranslinkLookupText);

        createTask(TASK_SEARCH_TRANSLINK, taskFindLocationProvider.get())
                .parameters(currentTranslinkLookupText)
                .start(this);
    }

    @OnClick(R.id.resolve_list_clear)
    void clearSearch() {
        executeNewSearch(SearchType.CLEAR_SEARCH);
    }

    void executeNewSearch(@NonNull SearchType searchType) {
        Log.debug(TAG, "- executeNewSearch. searchType: " + searchType);

        // Cancel any outstanding search tasks.
        cancelCurrentTask(true);

        // This may be fired while transitioning to the map fragment.
        if (!isVisible()) {
            Log.warn(TAG, "executeNewSearch. fragment not visible");
            return;
        }

        UiMode newUIMode = UiMode.NORMAL;
        if (searchType == SearchType.TRANSLINK) {
            String searchText = mLocationText.getText().toString().trim();

            // Check that the input is valid.
            boolean isValid = searchText.length() >= 3;
            if (isValid) {
                // Check if there are at least 3 alphabet characters before starting the search.
                isValid = searchText.matches(".*[a-zA-Z]{3,}.*");
            }

            if (isValid) {
                newUIMode = UiMode.TRANSLINK_LOOKUP;
            }

        } else {
            // Clear the text from the search textbox.
            updateSearchText("", false);

            if (searchType == SearchType.COORDINATES && addressLookupLatLng != null) {
                newUIMode = UiMode.ADDRESS_LOOKUP;
            }
        }

        updateUi(newUIMode, null);

        // Execute the respective search.
        switch (newUIMode) {
            case ADDRESS_LOOKUP:
                createTask(TASK_GET_ADDRESS, getAddressesAsyncTaskProvider.get())
                        .parameters(addressLookupLatLng)
                        .start(this);
                break;

            case TRANSLINK_LOOKUP:
                translinkSearchHandler.sendMessageDelayed(Message.obtain(translinkSearchHandler), 300);
                break;
        }
    }

    void updateUi(@NonNull UiMode newUIMode, ArrayList<String> existingAddressList) {
        mCurrentUIMode = newUIMode;

        int normalUiVisibility = (mCurrentUIMode == UiMode.NORMAL) ? View.VISIBLE : View.GONE;
        mChooseMapButton.setVisibility(normalUiVisibility);
        mOpenContactsButton.setVisibility(normalUiVisibility);
        changeHistoryVisibility(normalUiVisibility);

        // An address lookup uses the hint instead of an actual string, but it must have the same behaviour.
        if (mCurrentUIMode.searchType == SearchType.COORDINATES || mLocationText.length() > 0) {
            mClearButton.setVisibility(View.VISIBLE);
            mMicrophoneButton.setVisibility(View.GONE);

        } else {
            if (isVoiceRecognitionAvailable) {
                mMicrophoneButton.setVisibility(View.VISIBLE);
            }
            mClearButton.setVisibility(View.GONE);
        }

        if (mCurrentUIMode == UiMode.SHOW_RESULTS) {
            mResultsContainer.setVisibility(View.VISIBLE);

        } else {
            // Clear the adapter data since no search is occurring.
            mResultsContainer.setVisibility(View.GONE);
            mResultAdapter.changeData(null);
        }

        if (mCurrentUIMode.searchType == SearchType.COORDINATES) {
            mLocationText.setHint(addressLookupLatLng.latitude + ", " + addressLookupLatLng.longitude);
            mLocationText.setEnabled(false);

        } else {
            mLocationText.setHint(getSearchHint());
            mLocationText.setEnabled(true);
        }

        Log.debug(TAG, "updateUi: " + mCurrentUIMode);

        // Show the searching label
        int searchViewStringRes = -1, errorViewStringRes = -1;
        if (mCurrentUIMode.isSearchMode) {
            if (!NetworkUtil.isOnline(getActivity(), false)) {
                errorViewStringRes = R.string.resolve_no_internet;

            } else {
                switch (mCurrentUIMode) {
                    case ADDRESS_LOOKUP:
                        searchViewStringRes = R.string.resolve_searching_for_address;
                        break;

                    case TRANSLINK_LOOKUP:
                        searchViewStringRes = R.string.resolve_searching_for_location;
                        break;

                    default:
                        errorViewStringRes = R.string.resolve_generic_error;
                        break;
                }
            }
        }

        if (searchViewStringRes != -1) {
            mSearchingContainer.setVisibility(View.VISIBLE);
            mSearchingLabel.setText(searchViewStringRes);
        } else {
            mSearchingContainer.setVisibility(View.GONE);
        }

        if (errorViewStringRes != -1) {
            mErrorContainer.setVisibility(View.VISIBLE);
            mErrorLabel.setText(errorViewStringRes);
        } else {
            mErrorContainer.setVisibility(View.GONE);
        }

        if (mCurrentUIMode == UiMode.SHOW_RESULTS) {
            updateSearchResults(existingAddressList);
        }
    }

    private void changeHistoryVisibility(int visibility) {
        // Only show the history container if history data exists, and we are not currently searching.
        if (visibility == View.VISIBLE) {
            if (mHistoryAdapter.getItemCount() > 0 && !isTaskRunning()) {
                mHistoryContainer.setVisibility(View.VISIBLE);
            }
        } else {
            mHistoryContainer.setVisibility(visibility);
        }
    }

    void updateSearchResults(ArrayList<String> addressList) {
        int size = (addressList != null ? addressList.size() : 0);
        Log.debug(TAG, "updateSearchResults. count: " + size);
        //mLocationSearchTask = null;

        mResultAdapter.changeData(addressList);

        mResultsContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.resolve_choose_on_map)
    void openMap() {
        getBus().post(new ResolveLocationEvents.ShowMapEvent());
    }

    @OnClick(R.id.resolve_open_contacts)
    void openContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
        startActivityForResult(intent, CONTACTS_REQUEST);
    }

    @OnClick(R.id.resolve_error_retry_button)
    void retrySearch() {
        switch (mCurrentUIMode) {
            case ADDRESS_ERROR:
                executeNewSearch(SearchType.COORDINATES);
                break;

            case TRANSLINK_ERROR:
                executeNewSearch(SearchType.TRANSLINK);
                break;
        }
    }

    private void updateStreet(String street) {
        Log.debug(TAG, "updateStreet: " + street);
        if (street.contains("-")) {
            street = street.substring(street.indexOf("-") + 1);
        }

        updateSearchText(street, true);
    }

    private void updateSearchText(String newText, boolean executeSearch) {
        mLocationText.removeTextChangedListener(mSearchTextWatcher);
        mLocationText.setText(newText);
        mLocationText.addTextChangedListener(mSearchTextWatcher);

        if (executeSearch) {
            updateSearch(SearchType.TRANSLINK);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CONTACTS_REQUEST:
                    String address = contactAddressExtractor.getContactsAddress(getActivity(), data.getData());
                    if (address != null) {
                        updateStreet(address);
                    }

                    break;

                case MICROPHONE_REQUEST:
                    ArrayList<String> whatWasSpoken = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (whatWasSpoken != null && whatWasSpoken.size() > 0) {
                        updateStreet(whatWasSpoken.get(0));
                    }
            }
        }
    }

    public void setMapLookupPoint(LatLng point) {
        addressLookupLatLng = point;

        // If the fragment hasn't been inflated yet, this will be done elsewhere.
        if (mLocationText == null) {
            return;
        }

        updateLookupAddressInternal(addressLookupLatLng);
    }

    private boolean updateLookupAddressInternal(LatLng point) {
        Log.debug(TAG, "updateLookupAddressInternal. point: " + point);

        addressLookupLatLng = point;

        if (point != null) {
            updateSearch(SearchType.COORDINATES);
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        if (!isVisible()) {
            Log.warn(TAG, "onTaskFinished. fragment not visible");
            return;
        }

        switch (taskId) {
            case TASK_GET_ADDRESS:
                setMapLookupPoint(null);

                List<Address> addresses = (List<Address>) result.getItem();
                if (addresses.size() > 0) {
                    updateStreet(getAddressText(addresses.get(0)));
                } else {
                    Toast.makeText(getActivity(), "No results found.", Toast.LENGTH_SHORT).show();
                }
                break;

            case TASK_SEARCH_TRANSLINK:
                currentTranslinkLookupText = null;

                ArrayList<String> translinkAddressList = (ArrayList<String>) result.getItem();
                updateUi(UiMode.SHOW_RESULTS, translinkAddressList);
                break;
        }
    }

    @Override
    public void onTaskCancelled(int taskId) {
        Log.debug(TAG, "onTaskCancelled: " + taskId);

        if (!isVisible()) {
            Log.warn(TAG, "onTaskCancelled. fragment not visible");
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
        if (!isVisible()) {
            Log.warn(TAG, "onTaskError. fragment not visible");
            return true;
        }

        if (taskId == TASK_SEARCH_TRANSLINK) {
            updateUi(UiMode.TRANSLINK_ERROR, null);

            /*if (error.getType() == TaskGenericErrorType.NETWORK_FAILURE) {
                return true;
            }*/

        } else if (taskId == TASK_GET_ADDRESS) {
            updateUi(UiMode.ADDRESS_ERROR, null);
        }

        return true;
    }

    private void sendSaveLocationSelectedEvent(String address) {
        if (address != null) {
            getBus().post(new ResolveLocationEvents.LocationSelectedEvent(placeType, address, false));
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

    enum UiMode {
        // Regular state.
        NORMAL,
        // Lookup states.
        ADDRESS_LOOKUP(SearchType.COORDINATES, true), TRANSLINK_LOOKUP(SearchType.TRANSLINK, true),
        // Lookup error states.
        ADDRESS_ERROR(SearchType.COORDINATES, true), TRANSLINK_ERROR(SearchType.TRANSLINK, true),
        // Results list state.
        SHOW_RESULTS;

        private final SearchType searchType;
        private final boolean isSearchMode;

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

    static abstract class LocationAdapter extends RecyclerView.Adapter<LocationViewHolder> {
        private ArrayList<String> addressList;
        private final LocationViewHolder.LocationClickListener clickListener;

        public LocationAdapter(LocationViewHolder.LocationClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
            return new LocationViewHolder(view, getTextResId(), clickListener);
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            holder.bind(addressList.get(position));
        }

        @Override
        public int getItemCount() {
            if (addressList == null) {
                return 0;
            }
            return addressList.size();
        }

        public void changeData(ArrayList<String> addressList) {
            this.addressList = addressList;
            notifyDataSetChanged();
        }

        public ArrayList<String> getAddressList() {
            return addressList;
        }

        abstract int getLayoutId();

        abstract int getTextResId();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final LocationClickListener clickListener;

        private final TextView text;

        public LocationViewHolder(View itemView, int textResId, LocationClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;

            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(textResId);
        }

        public void bind(String address) {
            text.setText(address);
        }

        @Override
        public void onClick(View v) {
            clickListener.onResultClicked(getAdapterPosition());
        }

        public interface LocationClickListener {
            void onResultClicked(int position);
        }
    }

    static class ResultAdapter extends LocationAdapter {

        public ResultAdapter(LocationViewHolder.LocationClickListener clickListener) {
            super(clickListener);
        }

        @Override
        int getLayoutId() {
            return R.layout.l_resolve_location_result;
        }

        @Override
        int getTextResId() {
            return R.id.resolve_location_result_text;
        }
    }

    static class HistoryAdapter extends LocationAdapter {

        public HistoryAdapter(LocationViewHolder.LocationClickListener clickListener) {
            super(clickListener);
        }

        @Override
        int getLayoutId() {
            return R.layout.l_resolve_location_history;
        }

        @Override
        int getTextResId() {
            return R.id.resolve_location_history_text;
        }
    }
}