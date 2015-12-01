package com.lach.common.data.preference;

import android.support.annotation.NonNull;

public class LongPreference extends Preference<Long> {
    public LongPreference(String key, @NonNull Long defaultValue) {
        super(key, defaultValue);
    }

    @NonNull
    @Override
    public Long get(Preferences preferences) {
        return preferences.getLong(key, defaultValue);
    }

    @Override
    public void set(Preferences.Editor editor, @NonNull Long value) {
        editor.putLong(key, value);
    }
}