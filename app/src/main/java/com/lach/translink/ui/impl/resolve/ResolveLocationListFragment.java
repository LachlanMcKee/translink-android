package com.lach.translink.ui.impl.resolve;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.data.provider.ContactAddressExtractor;
import com.lach.common.log.Log;
import com.lach.common.ui.widget.WrapLinearLayoutManager;
import com.lach.common.util.NetworkUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.location.PlaceType;
import com.lach.common.data.map.MapPosition;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenter;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.ui.view.resolve.ResolveLocationListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveLocationListFragment extends AsyncTaskFragment implements ResolveLocationListView {
    private static final String TAG = "ResolveLocationListFragment";

    private static final int CONTACTS_REQUEST = 1;
    private static final int MICROPHONE_REQUEST = 2;

    private static final String PLACE_TYPE = "place_type";

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

    private ResultAdapter mResultAdapter;
    private HistoryAdapter mHistoryAdapter;

    private boolean isVoiceRecognitionAvailable;

    private TextWatcher mSearchTextWatcher;

    @Inject
    ResolveLocationListPresenter mPresenter;

    @Inject
    ContactAddressExtractor contactAddressExtractor;

    public static ResolveLocationListFragment newInstance(PlaceType placeType) {
        ResolveLocationListFragment f = new ResolveLocationListFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable(PLACE_TYPE, placeType);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_resolve_location_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.debug(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = getActivity();
        TranslinkApplication application = (TranslinkApplication) activity.getApplication();
        application.getDataComponent().inject(this);

        // Init the results list.
        final WrapLinearLayoutManager resultsLayoutManager = new WrapLinearLayoutManager(activity);
        resultsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mResultsList.setLayoutManager(resultsLayoutManager);

        mResultAdapter = new ResultAdapter(new LocationViewHolder.LocationClickListener() {
            @Override
            public void onResultClicked(int position) {
                mPresenter.sendSaveLocationSelectedEvent(mResultAdapter.getAddressList().get(position).data);
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
                mPresenter.sendSaveLocationSelectedEvent(mHistoryAdapter.getAddressList().get(position).data);
            }
        });

        mHistoryList.setAdapter(mHistoryAdapter);

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
                mPresenter.onSearchTextChanged(text.toString());
            }
        };

        mPresenter.setPlaceType((PlaceType) getArguments().getSerializable(PLACE_TYPE));
        mPresenter.onCreate(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.debug(TAG, "onSaveInstanceState");

        if (mPresenter != null) {
            mPresenter.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CONTACTS_REQUEST:
                    String address = contactAddressExtractor.getContactsAddress(getActivity(), data.getData());
                    if (address != null) {
                        mPresenter.updateStreet(address);
                    }

                    break;

                case MICROPHONE_REQUEST:
                    ArrayList<String> whatWasSpoken = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (whatWasSpoken != null && whatWasSpoken.size() > 0) {
                        mPresenter.updateStreet(whatWasSpoken.get(0));
                    }
            }
        }
    }

    @Override
    public void toggleKeyboard(boolean visible) {
        int mode;
        if (visible) {
            mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        } else {
            mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        }
        getActivity().getWindow().setSoftInputMode(mode);
    }

    @Override
    public void updateHistory(List<LocationInfo> history) {
        if (mHistoryAdapter == null) {
            return;
        }
        mHistoryAdapter.changeData(history);
        changeHistoryVisibility(View.VISIBLE);
    }

    @Override
    public void updateSearchResults(List<LocationInfo> addressList) {
        mResultAdapter.changeData(addressList);
        mResultsContainer.setVisibility(addressList != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateSearchText(String search) {
        mLocationText.setText(search);
    }

    @Override
    public void toggleSearchListener(boolean useListener) {
        if (useListener) {
            mLocationText.addTextChangedListener(mSearchTextWatcher);
        } else {
            mLocationText.removeTextChangedListener(mSearchTextWatcher);
        }
    }

    @Override
    public String getSearchText() {
        if (mLocationText == null) {
            return null;
        }
        return mLocationText.getText().toString().trim();
    }

    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        mPresenter.onTaskFinished(taskId, result);
    }

    @Override
    public void onTaskCancelled(int taskId) {
        mPresenter.onTaskCancelled(taskId);
    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        return mPresenter.onTaskError(taskId, errorId);
    }

    @Override
    public void updateUi(@NonNull ResolveLocationListPresenterImpl.UiMode uiMode) {
        int normalUiVisibility = (uiMode == ResolveLocationListPresenterImpl.UiMode.NORMAL) ? View.VISIBLE : View.GONE;
        mChooseMapButton.setVisibility(normalUiVisibility);
        mOpenContactsButton.setVisibility(normalUiVisibility);
        changeHistoryVisibility(normalUiVisibility);

        // An address lookup uses the hint instead of an actual string, but it must have the same behaviour.
        if (uiMode.searchType == ResolveLocationListPresenterImpl.SearchType.COORDINATES || mLocationText.length() > 0) {
            mClearButton.setVisibility(View.VISIBLE);
            mMicrophoneButton.setVisibility(View.GONE);

        } else {
            if (isVoiceRecognitionAvailable) {
                mMicrophoneButton.setVisibility(View.VISIBLE);
            }
            mClearButton.setVisibility(View.GONE);
        }

        if (uiMode == ResolveLocationListPresenterImpl.UiMode.SHOW_RESULTS) {
            mResultsContainer.setVisibility(View.VISIBLE);
        }

        Log.debug(TAG, "updateUi: " + uiMode);

        // Show the searching label
        int searchViewStringRes = -1, errorViewStringRes = -1;
        if (uiMode.isSearchMode) {
            if (!NetworkUtil.isOnline(getActivity(), false)) {
                errorViewStringRes = R.string.resolve_no_internet;

            } else {
                switch (uiMode) {
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
    }

    @Override
    public void updateSearchMode(boolean isEnabled, String hint) {
        if (mLocationText == null) {
            return;
        }
        mLocationText.setEnabled(isEnabled);
        mLocationText.setHint(hint);
    }

    @Override
    public void showNotification(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isUiReady() {
        return isAdded();
    }

    @Override
    public boolean isUiVisible() {
        return isVisible();
    }

    @OnClick(R.id.resolve_list_back_button)
    void goBack() {
        // Ensure no outstanding tasks are running.
        cancelCurrentTask(true);

        getActivity().finish();
    }

    @OnClick(R.id.resolve_error_retry_button)
    void retrySearch() {
        mPresenter.retrySearch();
    }

    @OnClick(R.id.resolve_list_clear)
    void clearSearch() {
        mPresenter.executeNewSearch(ResolveLocationListPresenterImpl.SearchType.CLEAR_SEARCH);
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

    public boolean handleBackPressed() {
        if (mLocationText.length() > 0) {
            clearSearch();
            return true;
        }
        goBack();
        return true;
    }

    public void setMapLookupPoint(MapPosition point) {
        if (mPresenter != null) {
            mPresenter.setMapLookupPoint(point);
        }
    }

    static abstract class LocationAdapter extends RecyclerView.Adapter<LocationViewHolder> {
        private List<LocationInfo> addressList;
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
            holder.bind(addressList.get(position).label);
        }

        @Override
        public int getItemCount() {
            if (addressList == null) {
                return 0;
            }
            return addressList.size();
        }

        public void changeData(List<LocationInfo> addressList) {
            this.addressList = addressList;
            notifyDataSetChanged();
        }

        public List<LocationInfo> getAddressList() {
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