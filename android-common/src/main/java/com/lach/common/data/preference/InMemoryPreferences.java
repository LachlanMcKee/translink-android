package com.lach.common.data.preference;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryPreferences implements Preferences {
    Map<String, Object> preferenceMap;

    public InMemoryPreferences() {
        preferenceMap = new HashMap<>();
    }

    private <T> T getValue(String key, T defValue) {
        Object value = preferenceMap.get(key);
        return (value != null) ? (T) value : defValue;
    }

    @Override
    public boolean contains(String key) {
        return preferenceMap.containsKey(key);
    }

    @Override
    public String getString(String key, String defValue) {
        return getValue(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return getValue(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return getValue(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return getValue(key, defValue);
    }

    @Override
    public Editor edit() {
        return new AndroidPreferencesEditor();
    }

    public class AndroidPreferencesEditor implements Editor {
        Map<String, Object> tempChanges;
        List<String> removedKeys;

        @SuppressLint("CommitPrefEdits")
        public AndroidPreferencesEditor() {
            tempChanges = new HashMap<>();
            removedKeys = new ArrayList<>();
        }

        @Override
        public Editor putString(String key, @Nullable String value) {
            tempChanges.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            tempChanges.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            tempChanges.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            tempChanges.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            removedKeys.add(key);
            return this;
        }

        @Override
        public void apply() {
            // Remove any elements as required.
            for (String key : removedKeys) {
                preferenceMap.remove(key);
            }

            // Update any values as required.
            Set<String> changedKeys = tempChanges.keySet();
            for (String key : changedKeys) {
                preferenceMap.put(key, tempChanges.get(key));
            }
        }
    }
}
