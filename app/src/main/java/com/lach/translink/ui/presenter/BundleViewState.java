package com.lach.translink.ui.presenter;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class BundleViewState implements ViewState {
    Bundle bundle;

    public static BundleViewState wrapBundle(Bundle bundle) {
        return new BundleViewState(bundle);
    }

    private BundleViewState(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void putDouble(String key, double value) {
        bundle.putDouble(key, value);
    }

    @Override
    public void putString(String key, String value) {
        bundle.putString(key, value);
    }

    @Override
    public void putStringArrayList(String key, ArrayList<String> value) {
        bundle.putStringArrayList(key, value);
    }

    @Override
    public void putParcelable(String key, Parcelable value) {
        bundle.putParcelable(key, value);
    }

    @Override
    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        bundle.putParcelableArrayList(key, value);
    }

    @Override
    public void putSerializable(String key, Serializable value) {
        bundle.putSerializable(key, value);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        if (bundle == null) return defaultValue;
        return bundle.getDouble(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        if (bundle == null) return null;
        return bundle.getString(key);
    }

    @Override
    public ArrayList<String> getStringArrayList(String key) {
        if (bundle == null) return null;
        return bundle.getStringArrayList(key);
    }

    @Override
    public <T extends Parcelable> T getParcelable(String key) {
        if (bundle == null) return null;
        return bundle.getParcelable(key);
    }

    @Override
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        if (bundle == null) return null;
        return bundle.getParcelableArrayList(key);
    }

    @Override
    public Serializable getSerializable(String key) {
        if (bundle == null) return null;
        return bundle.getSerializable(key);
    }

    @Override
    public void remove(String key) {
        if (bundle != null) {
            bundle.remove(key);
        }
    }
}
