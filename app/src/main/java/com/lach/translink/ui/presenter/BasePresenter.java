package com.lach.translink.ui.presenter;

import android.os.Bundle;

import com.lach.translink.ui.view.BaseView;

public interface BasePresenter<VIEW extends BaseView> {
    void setView(VIEW view);

    void removeView();

    void onCreate(Bundle savedInstanceState);

    void onDestroy();

    void onSaveInstanceState(Bundle outState);
}
