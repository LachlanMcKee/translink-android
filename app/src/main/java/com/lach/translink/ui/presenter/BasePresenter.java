package com.lach.translink.ui.presenter;

import com.lach.translink.ui.view.BaseView;

public interface BasePresenter<VIEW extends BaseView> {
    void onCreate(VIEW view, ViewState viewState);

    void onDestroy();

    void saveState(ViewState viewState);
}
