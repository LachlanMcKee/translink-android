package com.lach.translink.ui.view.resolve;

import com.google.android.gms.maps.model.LatLngBounds;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.common.data.map.MapMarker;
import com.lach.common.data.map.MapPosition;
import com.lach.translink.ui.presenter.resolve.ResolveLocationMapPresenterImpl;
import com.lach.translink.ui.view.TaskView;

public interface ResolveLocationMapView<MARKER_TYPE> extends TaskView {
    boolean isMapReady();

    float getMapZoomLevel();

    LatLngBounds getMapBounds();

    void showContinueButton(boolean animate);

    MapMarker<MARKER_TYPE> addMarker(MapPosition mapPosition);

    void hideContinueButton();

    MapMarker<MARKER_TYPE> addBusStopMarker(MapPosition mapPosition, boolean isSelected);

    double calculateBusStopLatitudeOffset();

    void showBusStopDetails(BusStop busStop, boolean animate);

    void moveCameraToBusStop(BusStop busStop, boolean animateCamera, ResolveLocationMapPresenterImpl.MoveCameraListener listener);

    void removeMapMarker(MapMarker<MARKER_TYPE> marker);

    void bringMarkerToFront(MapMarker<MARKER_TYPE> marker);
}
