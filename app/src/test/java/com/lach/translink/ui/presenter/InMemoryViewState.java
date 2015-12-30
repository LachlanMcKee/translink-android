package com.lach.translink.ui.presenter;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryViewState implements ViewState {
    Map<String, Object> map = new HashMap<>();

    @Override
    public void putDouble(String key, double value) {
        map.put(key, value);
    }

    @Override
    public void putString(String key, String value) {
        map.put(key, value);
    }

    @Override
    public void putStringArrayList(String key, ArrayList<String> value) {
        map.put(key, value);
    }

    @Override
    public void putParcelable(String key, Parcelable value) {
        map.put(key, value);
    }

    @Override
    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        map.put(key, value);
    }

    @Override
    public void putSerializable(String key, Serializable value) {
        map.put(key, value);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (double) value;
    }

    @Override
    public String getString(String key) {
        return (String) map.get(key);
    }

    @Override
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList<String>) map.get(key);
    }

    @Override
    public <T extends Parcelable> T getParcelable(String key) {
        return (T) map.get(key);
    }

    @Override
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        return (ArrayList<T>) map.get(key);
    }

    @Override
    public Serializable getSerializable(String key) {
        return (Serializable) map.get(key);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }
}
