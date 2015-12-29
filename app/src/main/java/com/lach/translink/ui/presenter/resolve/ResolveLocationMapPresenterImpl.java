package com.lach.translink.ui.presenter.resolve;

import android.os.Bundle;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.map.MapMarker;
import com.lach.common.data.map.MapPosition;
import com.lach.common.log.Log;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.tasks.place.TaskGetBusStops;
import com.lach.translink.ui.impl.resolve.ResolveLocationEvents;
import com.lach.translink.ui.view.resolve.ResolveLocationMapView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import de.greenrobot.event.EventBus;

public class ResolveLocationMapPresenterImpl implements ResolveLocationMapPresenter {
    private static final String TAG = ResolveLocationMapPresenterImpl.class.getSimpleName();

    private static final int TASK_GET_BUS_STOPS = 0;
    private static final float BUS_STOP_MIN_ZOOM = 15.0f;

    private static final String BUNDLE_CURRENT_MARKER_LATITUDE = "current_marker_latitude";
    private static final String BUNDLE_CURRENT_MARKER_LONGITUDE = "current_marker_longitude";
    private static final String BUNDLE_SELECTED_BUS_STOP = "selected_bus_stop";

    private final Provider<TaskGetBusStops> getBusStopsTaskProvider;

    private ResolveLocationMapView mView;

    private MapPosition mAddressMarkerPosition;
    private MapMarker mAddressMarker;
    private BusStop mSelectedBusStop = null;
    private Map<Long, BusStopContent> mVisibleBusStops;
    private Map<String, Long> mMarkerBusStopRelation;

    @Inject
    public ResolveLocationMapPresenterImpl(Provider<TaskGetBusStops> getBusStopsTaskProvider) {
        this.getBusStopsTaskProvider = getBusStopsTaskProvider;
    }

    @Override
    public void onCreate(ResolveLocationMapView view, Bundle savedInstanceState) {
        mView = view;

        mVisibleBusStops = new HashMap<>();
        mMarkerBusStopRelation = new HashMap<>();

        if (savedInstanceState != null) {
            double addressLookupLatitude = savedInstanceState.getDouble(BUNDLE_CURRENT_MARKER_LATITUDE, -1);
            double addressLookupLongitude = savedInstanceState.getDouble(BUNDLE_CURRENT_MARKER_LONGITUDE, -1);
            if (addressLookupLatitude != -1 && addressLookupLongitude != -1) {
                mAddressMarkerPosition = new MapPosition(addressLookupLatitude, addressLookupLongitude);
            }

            mSelectedBusStop = savedInstanceState.getParcelable(BUNDLE_SELECTED_BUS_STOP);

            // Remove the property, as un-marshalling this seems to cause issues with the map view onCreate.
            savedInstanceState.remove(BUNDLE_SELECTED_BUS_STOP);
        }

        view.hideContinueButton();
    }

    @Override
    public void onDestroy() {
        mAddressMarker = null;
        mSelectedBusStop = null;

        mVisibleBusStops.clear();
        mVisibleBusStops = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAddressMarkerPosition != null) {
            outState.putDouble(BUNDLE_CURRENT_MARKER_LATITUDE, mAddressMarkerPosition.getLatitude());
            outState.putDouble(BUNDLE_CURRENT_MARKER_LONGITUDE, mAddressMarkerPosition.getLongitude());
        }
        outState.putParcelable(BUNDLE_SELECTED_BUS_STOP, mSelectedBusStop);
    }

    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        if (!mView.isMapReady()) {
            return;
        }

        //
        // Get the current keys, we can prevent creating new markers, and remove old ones.
        //
        Set<Long> existingKeys = new HashSet<>(mVisibleBusStops.keySet());

        //noinspection unchecked
        List<BusStop> busStops = (List<BusStop>) result.getItem();
        for (BusStop stop : busStops) {
            long stopId = stop.getId();

            if (existingKeys.contains(stopId)) {
                //
                // If the marker already exists, don't create a new one. Remove the id as we delete
                // the markers matching the remaining keys.
                //
                existingKeys.remove(stopId);
                continue;
            }

            // We may be re-adding the currently selected bus stop's marker.
            boolean isMarkerSelected = mSelectedBusStop != null && mSelectedBusStop.getId() == stopId;
            MapMarker marker = mView.addBusStopMarker(new MapPosition(stop.getLatitude(), stop.getLongitude()), isMarkerSelected);

            // Add the marker for later reference.
            mVisibleBusStops.put(stopId, new BusStopContent(stop, marker));
            mMarkerBusStopRelation.put(marker.getId(), stopId);
        }

        // Remove the unused markers.
        for (Long oldBusStopId : existingKeys) {
            BusStopContent content = mVisibleBusStops.get(oldBusStopId);

            if (content != null) {
                mMarkerBusStopRelation.remove(content.marker.getId());
                mView.removeMapMarker(content.marker);
            }

            mVisibleBusStops.remove(oldBusStopId);
        }

        if (mSelectedBusStop != null) {
            selectBusStopMarker(mSelectedBusStop.getId(), false);
        }
    }

    @Override
    public void onTaskCancelled(int taskId) {

    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        return false;
    }

    @Override
    public void onMapInit() {
        if (mAddressMarkerPosition != null) {
            onMapClick(mAddressMarkerPosition);
        }
    }

    @Override
    public void onMapClick(MapPosition latLng) {
        showContinueButton();
        clearSelectedBusStop();

        // Remove the previous marker if it exists.
        clearAddressMarker();

        mAddressMarkerPosition = latLng;
        mAddressMarker = mView.addMarker(latLng);
    }

    private void showContinueButton() {
        mView.showContinueButton(mAddressMarker == null && mSelectedBusStop == null);
    }

    private void clearSelectedBusStop() {
        if (mSelectedBusStop != null) {
            replaceBusStopMarker(mSelectedBusStop.getId(), false);

            mSelectedBusStop = null;
        }
    }

    @Override
    public void onBusStopDismissed() {
        mView.hideContinueButton();
        clearSelectedBusStop();
        updateBusStopMarkers();
    }

    @Override
    public void onCameraChange() {
        updateBusStopMarkers();
    }

    private MapMarker replaceBusStopMarker(long busStopId, boolean isSelected) {
        BusStopContent busStopContent = mVisibleBusStops.get(busStopId);
        if (busStopContent == null) {
            return null;
        }

        MapMarker oldMarker = busStopContent.marker;
        MapMarker newMarker = mView.addBusStopMarker(oldMarker.getMapPosition(), isSelected);

        mMarkerBusStopRelation.remove(oldMarker.getId());
        mMarkerBusStopRelation.put(newMarker.getId(), busStopId);

        busStopContent.marker = newMarker;

        mView.removeMapMarker(oldMarker);

        return newMarker;
    }

    @Override
    public MapPosition getPersistedMapPosition() {
        if (mSelectedBusStop != null) {
            return new MapPosition(mSelectedBusStop.getLatitude() - mView.calculateBusStopLatitudeOffset(), mSelectedBusStop.getLongitude());
        }
        return null;
    }

    @Override
    public void onConfirmed() {
        if (mAddressMarkerPosition != null) {
            EventBus.getDefault().post(new ResolveLocationEvents.MapAddressSelectedEvent(mAddressMarkerPosition));

        } else if (mSelectedBusStop != null) {
            EventBus.getDefault().post(new ResolveLocationEvents.MapBusStopSelectedEvent(mSelectedBusStop));
        }
    }

    @Override
    public void onMarkerClick(String id) {
        clearSelectedBusStop();

        Long newlySelectedBusStopId = mMarkerBusStopRelation.get(id);

        if (newlySelectedBusStopId == null) {
            Log.warn(TAG, "mSelectedBusStopId not found.");
            return;
        }

        clearAddressMarker();
        selectBusStopMarker(newlySelectedBusStopId, true);
    }

    private void selectBusStopMarker(long busStopId, boolean moveCamera) {
        MapMarker newMarker = replaceBusStopMarker(busStopId, true);
        if (newMarker == null) {
            return;
        }

        mView.bringMarkerToFront(newMarker);

        boolean isBusStopAlreadySelected = (mSelectedBusStop != null);
        showContinueButton();

        BusStopContent busStopContent = mVisibleBusStops.get(busStopId);
        mSelectedBusStop = busStopContent.busStop;

        if (isBusStopAlreadySelected) {
            mView.showBusStopDetails(mSelectedBusStop, false);
        }

        if (!moveCamera) {
            return;
        }

        Log.debug(TAG, "Animating camera to bus stop marker");

        MoveCameraListener moveCameraListener = null;
        boolean animateCamera = false;

        if (!isBusStopAlreadySelected) {
            // Only animate if this activity is in the foreground.
            animateCamera = true;

            moveCameraListener = new MoveCameraListener() {
                @Override
                public void onFinish() {
                    mView.showBusStopDetails(mSelectedBusStop, true);
                }

                @Override
                public void onCancel() {

                }
            };
        }

        mView.moveCameraToBusStop(mSelectedBusStop, animateCamera, moveCameraListener);
    }

    private void clearAddressMarker() {
        if (mAddressMarker != null) {
            mView.removeMapMarker(mAddressMarker);
            mAddressMarker = null;
            mAddressMarkerPosition = null;
        }
    }

    private void updateBusStopMarkers() {
        if (!mView.isMapReady() || mView.getMapZoomLevel() < BUS_STOP_MIN_ZOOM) {
            return;
        }

        boolean taskRunning = mView.isTaskRunning();
        Log.debug(TAG, "onCameraChange. Zoom valid. Task running: " + taskRunning);

        if (taskRunning) {
            mView.cancelCurrentTask(false);
        }

        mView.createTask(TASK_GET_BUS_STOPS, getBusStopsTaskProvider.get())
                .parameters(TaskGetBusStops.createParams(mView.getMapBounds()))
                .start();
    }

    private static class BusStopContent {
        final BusStop busStop;
        MapMarker marker;

        public BusStopContent(BusStop busStop, MapMarker marker) {
            this.busStop = busStop;
            this.marker = marker;
        }
    }

    public interface MoveCameraListener {
        void onFinish();

        void onCancel();
    }
}
