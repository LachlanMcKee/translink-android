package com.lach.common.data.common;

import com.lach.common.BuildConfig;
import com.lach.common.data.preference.StringPreference;

public class CommonPreference {
    public static final StringPreference THEME = new StringPreference(BuildConfig.PREF_THEME_KEY, BuildConfig.THEME_VALUE_LIGHT);
}
