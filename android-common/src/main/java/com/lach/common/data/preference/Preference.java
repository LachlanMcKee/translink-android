package com.lach.common.data.preference;

public abstract class Preference<T> {
    final String key;
    final T defaultValue;

    protected Preference(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public abstract T get(Preferences preferences);

    public abstract void set(Preferences.Editor editor, T value);

    public boolean exists(Preferences preferences) {
        return preferences.contains(key);
    }

    public void remove(Preferences.Editor editor) {
        editor.remove(key);
    }
}
