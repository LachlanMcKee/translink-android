package com.lach.common.data.preference;

public interface Preferences {

    boolean contains(String key);

    String getString(String key, String defValue);

    boolean getBoolean(String key, boolean defValue);

    long getLong(String key, long defValue);

    float getFloat(String key, float defValue);

    Editor edit();

    interface Editor {
        Editor putString(String key, String value);

        Editor putBoolean(String key, boolean value);

        Editor putLong(String key, long value);

        Editor putFloat(String key, float value);

        Editor remove(String key);

        void apply();
    }
}
