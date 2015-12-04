package com.lach.common;

import android.app.Activity;
import android.app.Application;

import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.PreferencesProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public abstract class BaseApplication extends Application {

    private static Bus mEventBus;

    public static Bus getEventBus() {
        return mEventBus;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mEventBus = new Bus(ThreadEnforcer.MAIN);
    }

    public void applyTheme(Activity activity, ThemeType themeType) {
        if (themeType == ThemeType.NONE) {
            return;
        }

        int themeResId;
        if (isLightTheme()) {
            switch (themeType) {
                case NO_ACTION_BAR:
                    themeResId = R.style.AppThemeLight_NoActionBar;
                    break;

                case STANDARD:
                default:
                    themeResId = R.style.AppThemeLight;
                    break;
            }
        } else {
            switch (themeType) {
                case NO_ACTION_BAR:
                    themeResId = R.style.AppThemeDark_NoActionBar;
                    break;

                case STANDARD:
                default:
                    themeResId = R.style.AppThemeDark;
                    break;
            }
        }
        activity.setTheme(themeResId);
    }

    public boolean isLightTheme() {
        return CommonPreference.THEME.get(getPreferencesProvider().getPreferences()).equals(BuildConfig.THEME_VALUE_LIGHT);
    }

    public enum ThemeType {
        STANDARD, NO_ACTION_BAR, NONE
    }

    public abstract PreferencesProvider getPreferencesProvider();

}
