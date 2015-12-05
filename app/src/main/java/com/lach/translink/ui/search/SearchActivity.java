package com.lach.translink.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.ui.BaseActivity;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.place.bus.BusStopDao;
import com.lach.translink.ui.place.BusStopUpdateActivity;

import javax.inject.Inject;

/**
 * This class must remain here to ensure that users don't lose their icons.
 */
public class SearchActivity extends BaseActivity {

    @Inject
    BusStopDao busStopDao;

    @Inject
    PreferencesProvider preferencesProvider;

    private String currentThemeName;
    private static final String SEARCH_FRAGMENT_TAG = "search";
    private static final String SEARCH_DATA_KEY = "data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_single_fragment);

        TranslinkApplication application = (TranslinkApplication) getApplication();
        application.getDataComponent().inject(this);

        Preferences preferences = preferencesProvider.getPreferences();
        currentThemeName = CommonPreference.THEME.get(preferences);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();

            Bundle savedData = getIntent().getBundleExtra(SEARCH_DATA_KEY);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, SearchFragment.newInstance(savedData), SEARCH_FRAGMENT_TAG);
            ft.commit();
        }

        if (busStopDao.getRowCount() == 0) {
            startActivity(new Intent(this, BusStopUpdateActivity.class));
        } else {
            initWebView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Preferences preferences = preferencesProvider.getPreferences();
        String themeName = CommonPreference.THEME.get(preferences);

        if (!themeName.equals(currentThemeName)) {

            // Delay recreating as it causes issues to immediately recreate during onResume or onStart.
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 1);
        }
    }

    /**
     * Creates a WebView (if required) which may initialise (depending on the underlying WebView)
     * certain one-time components.
     * <p/>
     * This call will send a delayed message to ensure the initial UI loads smoothly for the user.
     */
    private void initWebView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TranslinkApplication) getApplication()).initWebViewIfRequired(SearchActivity.this);
            }
        }, 500);
    }
}