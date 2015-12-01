package com.lach.common.data.preference;

import android.support.annotation.NonNull;

public class BooleanPreference extends Preference<Boolean> {
    public BooleanPreference(String key, @NonNull Boolean defaultValue) {
        super(key, defaultValue);
    }
    
    @NonNull
    @Override
    public Boolean get(Preferences preferences) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public void set(Preferences.Editor editor, @NonNull Boolean value) {
        editor.putBoolean(key, value);
    }
}