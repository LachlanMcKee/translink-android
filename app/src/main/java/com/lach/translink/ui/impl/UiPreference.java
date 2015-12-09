package com.lach.translink.ui.impl;

import com.lach.common.data.preference.BooleanPreference;
import com.lach.translink.activities.BuildConfig;

public class UiPreference {
    public static final BooleanPreference IGNORE_HISTORY_TIME = new BooleanPreference(BuildConfig.PREF_KEY_IGNORE_HISTORY_TIME, true);
    public static final BooleanPreference AUTOMATIC_KEYBOARD = new BooleanPreference(BuildConfig.PREF_KEY_AUTOMATIC_KEYBOARD, false);
}
