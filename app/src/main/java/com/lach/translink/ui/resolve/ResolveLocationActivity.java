package com.lach.translink.ui.resolve;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lach.common.ui.BaseActivity;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.location.history.LocationHistoryDao;
import com.lach.translink.data.location.PlaceType;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class ResolveLocationActivity extends BaseActivity {

    public static final String PLACE_TYPE_KEY = "placeType";
    public static final String ADDRESS_KEY = "location";
    public static final String IS_FAVOURITE_KEY = "favourite";

    private static final String TAG_LIST = "TAG_LIST";
    private static final String TAG_MAP = "TAG_MAP";

    private ResolveLocationListFragment listFragment;
    private PlaceType mPlaceType;

    @Inject
    LocationHistoryDao locationHistoryDao;

    public static Intent createIntent(Context context) {
        return createIntent(context, null);
    }

    public static Intent createIntent(Context context, PlaceType placeType) {
        Intent intent = new Intent(context, ResolveLocationActivity.class);
        intent.putExtra(PLACE_TYPE_KEY, placeType);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_single_fragment);

        TranslinkApplication application = (TranslinkApplication) getApplication();
        application.getDataComponent().inject(this);

        mPlaceType = (PlaceType) getIntent().getSerializableExtra(PLACE_TYPE_KEY);

        FragmentManager fm = getSupportFragmentManager();
        listFragment = (ResolveLocationListFragment) fm.findFragmentByTag(TAG_LIST);

        if (listFragment == null) {
            listFragment = ResolveLocationListFragment.newInstance(mPlaceType);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, listFragment, TAG_LIST);
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {
        listFragment = null;
        super.onDestroy();
    }

    @Subscribe
    public void onLocationSelected(ResolveLocationEvents.LocationSelectedEvent locationSelectedEvent) {
        String translinkAddress = locationSelectedEvent.getAddress();

        // Add the selected location to the location history.
        updateLocationHistory(translinkAddress);

        // Send the result back to the calling activity.
        Intent data = new Intent();
        data.putExtra(PLACE_TYPE_KEY, locationSelectedEvent.getPlaceType());
        data.putExtra(ADDRESS_KEY, translinkAddress);
        data.putExtra(IS_FAVOURITE_KEY, locationSelectedEvent.isFavourite());

        setResult(Activity.RESULT_OK, data);

        finish();
        overridePendingTransition(0, 0);
    }

    @Subscribe
    public void onShowMap(ResolveLocationEvents.ShowMapEvent event) {
        hideKeyboard();

        ResolveLocationMapFragment mapFragment = ResolveLocationMapFragment.newInstance(mPlaceType);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, mapFragment, TAG_MAP);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Subscribe
    public void onMapMarkerSelected(ResolveLocationEvents.MapMarkerSelectedEvent event) {
        // Remove the map fragment from the back stack.
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();

        if (listFragment != null) {
            listFragment.setMapLookupPoint(event.getPoint());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (listFragment != null && listFragment.isVisible()) {
                if (listFragment.handleBackPressed()) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean isActionBarUsed() {
        return false;
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateLocationHistory(String translinkAddress) {
        locationHistoryDao.insertOrUpdateAddress(translinkAddress);
    }
}
