package com.lach.translink.ui.result;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.lach.common.ui.BaseActivity;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.JourneyCriteria;

import java.util.Date;

public class JourneyResultActivity extends BaseActivity implements JourneyResultFragment.JourneyResultListener {

    private static final String RESULT_FRAGMENT_TAG = "result";

    public static final String JOURNEY_CRITERIA = "journey";
    public static final String JOURNEY_DATE = "date";

    private static final String CONTENT_LOADED_KEY = "content_loaded";

    private JourneyResultFragment resultsFragment;
    private boolean contentLoaded = false;

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //ThemeHelper.applyThemeTransparent(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_journey_result);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (savedInstanceState != null) {
            contentLoaded = savedInstanceState.getBoolean(CONTENT_LOADED_KEY);
        }
        updateToolbarVisibility();

        // Add the action bar back button.
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        resultsFragment = (JourneyResultFragment) fm.findFragmentByTag(RESULT_FRAGMENT_TAG);

        if (resultsFragment == null) {
            JourneyCriteria journeyCriteria = getIntent().getParcelableExtra(JOURNEY_CRITERIA);
            Date date = (Date) getIntent().getSerializableExtra(JOURNEY_DATE);
            resultsFragment = JourneyResultFragment.newInstance(journeyCriteria, date);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_journey_result, resultsFragment, RESULT_FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CONTENT_LOADED_KEY, contentLoaded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && resultsFragment.handleBackPressed()) {
            return true;
        }
        finish();
        overridePendingTransition(0, 0);
        return true;
    }

    @Override
    public void onContentLoaded() {
        contentLoaded = true;
        updateToolbarVisibility();
    }

    private void updateToolbarVisibility() {
        toolbar.setVisibility(contentLoaded ? View.VISIBLE : View.INVISIBLE);
    }
}