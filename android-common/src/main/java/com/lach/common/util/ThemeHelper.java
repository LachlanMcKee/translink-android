package com.lach.common.util;

import android.app.Activity;
import android.content.Context;

import com.lach.common.R;
import com.lach.common.data.preference.Preferences;

public class ThemeHelper {

    public static void applyTheme(Activity activity, Preferences prefs, boolean useActionBar) {
        if (isLightTheme(activity, prefs)) {
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

    public static boolean isLightTheme(Context context, Preferences prefs) {
        String lightThemeType = context.getString(R.string.theme_light);
        String themePrefKey = context.getString(R.string.theme_pref_key);

        String themeType = prefs.getString(themePrefKey, lightThemeType);
        return themeType.equals(lightThemeType);
    }

}
