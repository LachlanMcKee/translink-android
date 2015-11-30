package com.lach.translink.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public abstract class PrimaryViewModel extends BaseViewModel {
    private final Context context;
    private FragmentActivity activity;
    private Fragment fragment;

    @SuppressWarnings("unused")
    public PrimaryViewModel(FragmentActivity activity) {
        super();
        this.activity = activity;
        this.context = activity;
    }

    public PrimaryViewModel(Fragment fragment) {
        super();
        this.fragment = fragment;
        this.context = fragment.getActivity();
    }

    public Context getContext() {
        return context;
    }

    public FragmentActivity getActivity() {
        if (activity != null) {
            return activity;
        }
        return fragment.getActivity();
    }

    public Fragment getFragment() {
        return fragment;
    }
}
