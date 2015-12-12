package com.lach.translink.ui.view.resolve;

import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.ui.view.TaskView;

import java.util.ArrayList;

public interface ResolveLocationListView extends TaskView {
    void toggleKeyboard(boolean visible);

    void updateHistory(ArrayList<String> history);

    void updateSearchResults(ArrayList<String> addressList);

    void updateSearchText(String search);

    void toggleSearchListener(boolean useListener);

    String getSearchText();

    void updateUi(ResolveLocationListPresenterImpl.UiMode uiMode);

    void updateSearchMode(boolean isEnabled, String hint);

    void showNotification(String message);
}
