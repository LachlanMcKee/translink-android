package com.lach.translink.ui.view.resolve;

import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.async.Task;
import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.ui.view.BaseView;

import java.util.ArrayList;

public interface ResolveLocationListView extends BaseView {
    void toggleKeyboard(boolean visible);

    void updateHistory(ArrayList<String> history);

    void updateSearchResults(ArrayList<String> addressList);

    void updateSearchText(String search);

    void toggleSearchListener(boolean useListener);

    String getSearchText();

    void updateUi(ResolveLocationListPresenterImpl.UiMode uiMode, ArrayList<String> existingAddressList);

    void updateSearchMode(boolean isEnabled, String hint);

    void showNotification(String message);

    AsyncTaskFragment.TaskBuilder createTask(int taskSearchTranslink, Task task);

    void cancelCurrentTask(boolean notify);
}
