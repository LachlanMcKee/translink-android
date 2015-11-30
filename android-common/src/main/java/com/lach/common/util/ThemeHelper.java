package com.lach.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lach.common.R;

public class ThemeHelper {

    public static void applyTheme(Activity activity) {
        applyTheme(activity, true);
    }

    public static void applyTheme(Activity activity, boolean useActionBar) {
        if (isLightTheme(activity)) {
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

    public static boolean isLightTheme(Context c) {
        String lightThemeType = c.getString(R.string.theme_light);
        String themePrefKey = c.getString(R.string.theme_pref_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String themeType = prefs.getString(themePrefKey, lightThemeType);

        return themeType.equals(lightThemeType);
    }

}
