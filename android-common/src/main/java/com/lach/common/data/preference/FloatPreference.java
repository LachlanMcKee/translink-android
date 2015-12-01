package com.lach.common.data.preference;

import android.support.annotation.NonNull;

public class FloatPreference extends Preference<Float> {
    public FloatPreference(String key, @NonNull Float defaultValue) {
        super(key, defaultValue);
    }
    
    @NonNull
    @Override
    public Float get(Preferences preferences) {
        return preferences.getFloat(key, defaultValue);
    }

    @Override
    public void set(Preferences.Editor editor, @NonNull Float value) {
        editor.putFloat(key, value);
    }
}