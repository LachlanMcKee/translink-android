package com.lach.translink.ui.presenter;

import com.lach.common.async.AsyncTaskUi;
import com.lach.translink.ui.view.TaskView;

public interface TaskPresenter<VIEW extends TaskView> extends BasePresenter<VIEW>, AsyncTaskUi {
    void onCreate(VIEW view, ViewState viewState);

    void onDestroy();

    void saveState(ViewState viewState);
}
