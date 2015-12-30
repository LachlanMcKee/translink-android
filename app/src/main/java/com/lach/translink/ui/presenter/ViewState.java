package com.lach.translink.ui.presenter;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public interface ViewState {
    void putDouble(String key, double value);

    void putString(String key, String value);

    void putStringArrayList(String key, ArrayList<String> value);

    void putParcelable(String key, Parcelable value);

    void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value);

    void putSerializable(String key, Serializable value);

    double getDouble(String key, double defaultValue);

    String getString(String key);

    ArrayList<String> getStringArrayList(String key);

    <T extends Parcelable> T getParcelable(String key);

    <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key);

    Serializable getSerializable(String key);

    void remove(String key);
}
