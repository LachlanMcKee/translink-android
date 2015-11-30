package com.lach.common.data.preference;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

public class AndroidPreferences implements Preferences {

    private final SharedPreferences sharedPreferences;

    public AndroidPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public Editor edit() {
        return new AndroidPreferencesEditor();
    }

    public class AndroidPreferencesEditor implements Editor {

        private final SharedPreferences.Editor editor;

        @SuppressLint("CommitPrefEdits")
        public AndroidPreferencesEditor() {
            editor = sharedPreferences.edit();
        }

        @Override
        public Editor putString(String key, String value) {
            editor.putString(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            editor.putBoolean(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            editor.putLong(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            editor.putFloat(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            editor.remove(key);
            return this;
        }

        @Override
        public void apply() {
            editor.apply();
        }
    }
}
