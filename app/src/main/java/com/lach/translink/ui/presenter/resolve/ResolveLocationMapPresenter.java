package com.lach.translink.ui.presenter.resolve;

import com.lach.common.data.map.MapPosition;
import com.lach.translink.ui.presenter.TaskPresenter;
import com.lach.translink.ui.view.resolve.ResolveLocationMapView;

public interface ResolveLocationMapPresenter extends TaskPresenter<ResolveLocationMapView> {
    void onMapInit();

    void onMapClick(MapPosition mapPosition);

    void onMarkerClick(String id);

    void onConfirmed();

    void onBusStopDismissed();

    void onCameraChange();

    MapPosition getPersistedMapPosition();
}
