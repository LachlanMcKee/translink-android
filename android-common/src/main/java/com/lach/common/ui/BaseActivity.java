package com.lach.common.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lach.common.BaseApplication;
import com.lach.common.log.Log;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    private boolean mAllowCommit;
    private boolean isConfigChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the a dynamic theme to the activity if required.
        BaseApplication.ThemeType themeType = getThemeType();
        if (themeType != BaseApplication.ThemeType.NONE) {
            BaseApplication application = (BaseApplication) getApplication();
            application.applyTheme(this, themeType);
        }

        super.onCreate(savedInstanceState);
        Log.debug(TAG, "onCreate");

        mAllowCommit = true;

        ButterKnife.inject(this);
    }

    public BaseApplication.ThemeType getThemeType() {
        return BaseApplication.ThemeType.STANDARD;
    }

    private Bus getBus() {
        return BaseApplication.getEventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.debug(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug(TAG, "onResume");

        // Register ourselves so that we can provide the initial value.
        getBus().register(this);
    }

    @Override
    protected void onResumeFragments() {
        mAllowCommit = true;
        super.onResumeFragments();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug(TAG, "onPause");

        // Always unregister when an object no longer should be on the bus.
        getBus().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mAllowCommit = false;
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        mAllowCommit = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug(TAG, "onDestroy");
    }

    @Override
    public void recreate() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.recreate();
        } else {
            finish();
            startActivity(getRecreateIntent());
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        isConfigChange = true;
        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    public boolean isChangingConfigurations() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return super.isChangingConfigurations();
        }
        return isConfigChange;
    }

    public Intent getRecreateIntent() {
        return getIntent();
    }

    public boolean isFragmentCommitAllowed() {
        return mAllowCommit;
    }
}
