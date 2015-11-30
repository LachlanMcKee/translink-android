package com.lach.translink.ui.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.ui.BaseActivity;
import com.lach.common.util.ThemeHelper;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.util.DataResource;

/**
 * This class must remain here to ensure that users don't lose their icons.
 */
public class SearchActivity extends BaseActivity {

    private String currentThemeName;
    private static final String SEARCH_FRAGMENT_TAG = "search";
    private static final String SEARCH_DATA_KEY = "data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_single_fragment);

        new DataResource().migrate((TranslinkApplication) getApplication());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentThemeName = prefs.getString(getString(R.string.theme_pref_key), getString(R.string.theme_light));

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();

            Bundle savedData = getIntent().getBundleExtra(SEARCH_DATA_KEY);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, SearchFragment.newInstance(savedData), SEARCH_FRAGMENT_TAG);
            ft.commit();
        }

        initWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = prefs.getString(getString(R.string.theme_pref_key), getString(R.string.theme_light));

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