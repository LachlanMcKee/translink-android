package com.lach.translink.data.journey;

import com.lach.common.data.preference.StringPreference;
import com.lach.translink.activities.BuildConfig;

public class JourneyPreference {
    public static final StringPreference MAX_WALKING_DISTANCE =
            new StringPreference(BuildConfig.PREF_KEY_MAX_WALK, "1000");

    public static final StringPreference WALK_SPEED =
            new StringPreference(BuildConfig.PREF_KEY_WALK_SPEED, "Normal");

    public static final StringPreference SERVICE_TYPES =
            new StringPreference(BuildConfig.PREF_KEY_SERVICE_TYPES, "Regular~Express~NightLink");

    public static final StringPreference FARE_TYPES =
            new StringPreference(BuildConfig.PREF_KEY_FARE_TYPES, "Free~Standard~Prepaid");
}
