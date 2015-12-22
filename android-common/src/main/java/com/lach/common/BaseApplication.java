package com.lach.common;

import android.app.Activity;
import android.app.Application;

import com.lach.common.data.CoreComponent;
import com.lach.common.data.CoreModule;
import com.lach.common.data.DaggerCoreComponent;
import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.PreferencesProvider;

public abstract class BaseApplication extends Application {
    private CoreComponent coreComponent;
    private CoreModule coreModule;

    public synchronized CoreModule getCoreModule() {
        if (coreModule == null) {
            coreModule = new CoreModule(this);
        }
        return coreModule;
    }

    public synchronized CoreComponent getCoreComponent() {
        if (coreComponent == null) {
            coreComponent = DaggerCoreComponent.builder()
                    .coreModule(getCoreModule())
                    .build();
        }
        return coreComponent;
    }

    public void setCoreComponent(CoreComponent coreComponent) {
        this.coreComponent = coreComponent;
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
