package com.lach.translink.ui.view.resolve;

import com.lach.translink.ui.presenter.resolve.ResolveLocationListPresenterImpl;
import com.lach.translink.ui.view.TaskView;

import java.util.List;

public interface ResolveLocationListView extends TaskView {
    void toggleKeyboard(boolean visible);

    void updateHistory(List<LocationInfo> history);

    void updateSearchResults(List<LocationInfo> addressList);

    void updateSearchText(String search);

    void toggleSearchListener(boolean useListener);

    String getSearchText();

    void updateUi(ResolveLocationListPresenterImpl.UiMode uiMode);

    void updateSearchMode(boolean isEnabled, String hint);

    void showNotification(String message);

    class LocationInfo {
        public final String data;
        public final String label;

        public LocationInfo(String data, String label) {
            this.data = data;
            this.label = label;
        }
    }
}
