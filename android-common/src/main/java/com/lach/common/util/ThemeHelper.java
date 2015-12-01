package com.lach.common.util;

import android.app.Activity;

import com.lach.common.BuildConfig;
import com.lach.common.R;
import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.Preferences;

public class ThemeHelper {

    public static void applyTheme(Activity activity, Preferences prefs, boolean useActionBar) {
        if (isLightTheme(prefs)) {
            if (useActionBar) {
                activity.setTheme(R.style.AppThemeLight);
            } else {
                activity.setTheme(R.style.AppThemeLight_NoActionBar);
            }
        } else {
            if (useActionBar) {
                activity.setTheme(R.style.AppThemeDark);
            } else {
                activity.setTheme(R.style.AppThemeDark_NoActionBar);
            }
        }
    }

    public static boolean isLightTheme(Preferences prefs) {
        return CommonPreference.THEME.get(prefs).equals(BuildConfig.THEME_VALUE_LIGHT);
    }

}
