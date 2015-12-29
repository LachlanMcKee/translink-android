package com.lach.translink.ui.presenter;

import android.os.Bundle;

import com.lach.translink.ui.view.BaseView;

public interface BasePresenter<VIEW extends BaseView> {
    void onCreate(VIEW view, Bundle savedInstanceState);

    void onDestroy();

    void onSaveInstanceState(Bundle outState);
}
