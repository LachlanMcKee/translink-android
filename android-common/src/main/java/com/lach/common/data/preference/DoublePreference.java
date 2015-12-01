package com.lach.common.data.preference;

import android.support.annotation.NonNull;

public class DoublePreference extends Preference<Double> {
    public DoublePreference(String key, @NonNull Double defaultValue) {
        super(key, defaultValue);
    }
    
    @NonNull
    @Override
    public Double get(Preferences preferences) {
        return Double.longBitsToDouble(preferences.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    @Override
    public void set(Preferences.Editor editor, @NonNull Double value) {
        editor.putLong(key, Double.doubleToLongBits(value));
    }
}