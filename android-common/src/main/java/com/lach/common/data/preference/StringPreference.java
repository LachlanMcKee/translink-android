package com.lach.common.data.preference;

public class StringPreference extends Preference<String> {
    public StringPreference(String key, String defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public String get(Preferences preferences) {
        return preferences.getString(key, defaultValue);
    }

    @Override
    public void set(Preferences.Editor editor, String value) {
        editor.putString(key, value);
    }
}