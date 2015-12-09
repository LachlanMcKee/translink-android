package com.lach.translink.ui.impl.resolve;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.data.preference.BooleanPreference;
import com.lach.common.data.preference.DoublePreference;
import com.lach.common.data.preference.FloatPreference;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.log.Log;
import com.lach.common.ui.view.ScaleAnimator;
import com.lach.common.util.MapUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.tasks.place.TaskGetBusStops;
import com.lach.translink.ui.impl.UiPreference;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveLocationMapFragment extends AsyncTaskFragment implements GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "ResolveLocationMapFragment";

    private static final String PLACE_TYPE = "place_type";

    private static final int TASK_GET_BUS_STOPS = 0;

    private static final BooleanPreference PREF_POSITION_SET = new BooleanPreference("position_set", false);
    private static final DoublePreference PREF_LAT = new DoublePreference("lat", 0.0d);
    private static final DoublePreference PREF_LONG = new DoublePreference("long", 0.0d);
    private static final FloatPreference PREF_ZOOM = new FloatPreference("zoom", 0.0f);
    private static final FloatPreference PREF_BEARING = new FloatPreference("bearing", 0.0f);

    private static final String BUNDLE_CURRENT_MARKER_POSITION = "current_marker_position";
    private static final String BUNDLE_SELECTED_BUS_STOP = "selected_bus_stop";
    private static final String BUNDLE_LATITUDE_LENGTH_FOR_PIXEL = "latitude_length_for_pixel";
    private static final String BUNDLE_LATITUDE_LENGTH_ZOOM = "latitude_length_zoom";

    private static final CameraPosition BRISBANE =
            new CameraPosition.Builder().target(new LatLng(-27.4679400, 153.0280900))
                    .zoom(13.0f)
                    .bearing(0)
                    .build();

    private static final float BUS_STOP_MIN_ZOOM = 15.0f;
    private BitmapDescriptor busSelectedBitmapDescriptor;
    private BitmapDescriptor busUnselectedBitmapDescriptor;

    private GoogleMap mMap;
    private float latitudeLengthZoomLevel = 0.0f;
    private double latitudeLengthForPixel = 0.0d;
    private boolean ignoreFirstCameraChange;

    private LatLng mAddressMarkerPosition;
    private Marker mAddressMarker;
    private BusStop mSelectedBusStop = null;
    private Map<Long, BusStopContent> mVisibleBusStops;
    private Map<String, Long> mMarkerBusStopRelation;

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    Provider<TaskGetBusStops> getBusStopsTaskProvider;

    @InjectView(R.id.resolve_map_coordinator)
    ViewGroup coordinatorLayout;

    @InjectView(R.id.appbar)
    View appBar;

    @InjectView(R.id.resolve_title)
    TextView title;

    @InjectView(R.id.resolve_subtitle)
    TextView subtitle;

    @InjectView(R.id.resolve_map_continue)
    View continueButton;

    @InjectView(R.id.resolve_map_bus_stop_info)
    ResolveBusStopInfoView busStopInfoView;

    // Don't data-bind this. We need to reference it after onDestroyView.
    MapView mapView;

    public static ResolveLocationMapFragment newInstance(PlaceType placeType) {
        ResolveLocationMapFragment f = new ResolveLocationMapFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable(PLACE_TYPE, placeType);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_resolve_location_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        ButterKnife.inject(this, view);

        mVisibleBusStops = new HashMap<>();
        mMarkerBusStopRelation = new HashMap<>();

        // Reload the current marker position if it exists.
        if (savedInstanceState != null) {
            mAddressMarkerPosition = savedInstanceState.getParcelable(BUNDLE_CURRENT_MARKER_POSITION);

            mSelectedBusStop = savedInstanceState.getParcelable(BUNDLE_SELECTED_BUS_STOP);

            // Remove the property, as un-marshalling this seems to cause issues with the map view onCreate.
            savedInstanceState.remove(BUNDLE_SELECTED_BUS_STOP);

            latitudeLengthForPixel = savedInstanceState.getDouble(BUNDLE_LATITUDE_LENGTH_FOR_PIXEL, 0);
            latitudeLengthZoomLevel = savedInstanceState.getFloat(BUNDLE_LATITUDE_LENGTH_ZOOM, 0);
        }

        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.resolve_map_container);

        // Configure the map.
        mapView = new MapView(getActivity(), createGoogleMapOptions());

        mapViewContainer.addView(mapView);
        mapView.onCreate(savedInstanceState);
        MapUtil.moveLocationButtonToBottomRight(mapView, getResources().getDimensionPixelOffset(R.dimen.map_location_button_margin));

        Preferences preferences = preferencesProvider.getPreferences();
        if (UiPreference.AUTOMATIC_KEYBOARD.get(preferences)) {
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        // Set the appropriate title.
        PlaceType placeType = (PlaceType) getArguments().getSerializable(PLACE_TYPE);
        String description;
        if (placeType != null) {
            description = placeType.getDescription().toLowerCase();
        } else {
            description = "location";
        }
        title.setText("Choose " + description);

        hideContinueButton();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                initMapSettings(googleMap);

                if (mAddressMarkerPosition != null) {
                    setAddressMarker(mAddressMarkerPosition);
                }
            }
        });

        busStopInfoView.setDismissListener(new ResolveBusStopInfoView.DismissListener() {
            @Override
            public void onDismissCompleted() {
                hideContinueButton();

                clearSelectedBusStop();
                updateBusStopMarkers();

                busStopInfoView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        saveLastCameraPosition();

        if (mMap != null) {
            mMap.setMyLocationEnabled(false);
            mMap = null;
        }

        mAddressMarker = null;
        mSelectedBusStop = null;

        mVisibleBusStops.clear();
        mVisibleBusStops = null;

        ButterKnife.reset(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_CURRENT_MARKER_POSITION, mAddressMarkerPosition);

        outState.putParcelable(BUNDLE_SELECTED_BUS_STOP, mSelectedBusStop);

        outState.putDouble(BUNDLE_LATITUDE_LENGTH_FOR_PIXEL, latitudeLengthForPixel);
        outState.putFloat(BUNDLE_LATITUDE_LENGTH_ZOOM, latitudeLengthZoomLevel);

        super.onSaveInstanceState(outState);
    }

    private GoogleMapOptions createGoogleMapOptions() {
        GoogleMapOptions options = new GoogleMapOptions();

        Preferences preferences = preferencesProvider.getPreferences();
        boolean lastPositionSet = PREF_POSITION_SET.get(preferences);

        CameraPosition cameraPosition;
        if (lastPositionSet) {
            double latitude = PREF_LAT.get(preferences);
            double longitude = PREF_LONG.get(preferences);

            if (mSelectedBusStop != null) {
                latitude = mSelectedBusStop.getLatitude() - calculateBusStopLatitudeOffset();
                longitude = mSelectedBusStop.getLongitude();
            }

            float zoom = PREF_ZOOM.get(preferences);
            float bearing = PREF_BEARING.get(preferences);

            cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude))
                    .zoom(zoom)
                    .bearing(bearing)
                    .build();
        } else {
            cameraPosition = BRISBANE;
        }

        options.camera(cameraPosition);
        options.mapToolbarEnabled(false);
        return options;
    }

    private void initMapSettings(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity());

        mMap = googleMap;

        busSelectedBitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
        busUnselectedBitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        googleMap.setMyLocationEnabled(true);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnCameraChangeListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setInfoWindowAdapter(new HiddenMapWindowAdapter(getActivity()));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setAddressMarker(latLng);
    }

    private void setAddressMarker(LatLng latLng) {
        showContinueButton();
        clearSelectedBusStop();

        // Remove the previous marker if it exists.
        clearAddressMarker();

        mAddressMarkerPosition = latLng;
        mAddressMarker = mMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void clearAddressMarker() {
        if (mAddressMarker != null) {
            mAddressMarker.remove();
            mAddressMarker = null;
            mAddressMarkerPosition = null;
        }
    }

    private void showContinueButton() {
        boolean animate = (mAddressMarker == null && mSelectedBusStop == null);

        if (!animate) {
            continueButton.setVisibility(View.VISIBLE);
        }

        if (animate) {
            ScaleAnimator continueButtonScaler = new ScaleAnimator(continueButton);
            continueButtonScaler.show();
        }
        subtitle.setText(R.string.resolve_map_subtitle_continue);
    }

    private void hideContinueButton() {
        continueButton.setVisibility(View.GONE);
        subtitle.setText(R.string.resolve_map_subtitle_intro);
    }

    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        if (mMap == null) {
            return;
        }

        //
        // Get the current keys, we can prevent creating new markers, and remove old ones.
        //
        Set<Long> existingKeys = new HashSet<>(mVisibleBusStops.keySet());

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
            BitmapDescriptor bitmapDescriptor;
            if (mSelectedBusStop != null && mSelectedBusStop.getId() == stopId) {
                bitmapDescriptor = busSelectedBitmapDescriptor;
            } else {
                bitmapDescriptor = busUnselectedBitmapDescriptor;
            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                    .icon(bitmapDescriptor)
            );

            // Add the marker for later reference.
            mVisibleBusStops.put(stopId, new BusStopContent(stop, marker));
            mMarkerBusStopRelation.put(marker.getId(), stopId);
        }

        // Remove the unused markers.
        for (Long oldBusStopId : existingKeys) {
            BusStopContent content = mVisibleBusStops.get(oldBusStopId);

            if (content != null) {
                mMarkerBusStopRelation.remove(content.marker.getId());
                content.marker.remove();
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
    public void onDestroy() {
        // Always cancel the bus stop markers, as the new configuration may have a different boundary.
        cancelCurrentTask(false);

        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void saveLastCameraPosition() {
        if (mMap == null) {
            return;
        }

        CameraPosition position = mMap.getCameraPosition();

        // Save the current camera position for next time.
        Preferences preferences = preferencesProvider.getPreferences();
        Preferences.Editor editor = preferences.edit();
        PREF_POSITION_SET.set(editor, true);

        PREF_LAT.set(editor, position.target.latitude);
        PREF_LONG.set(editor, position.target.longitude);
        PREF_ZOOM.set(editor, position.zoom);
        PREF_BEARING.set(editor, position.bearing);

        editor.apply();
    }

    @OnClick(R.id.resolve_map_continue)
    void confirmAddressMarker() {
        if (mAddressMarkerPosition != null) {
            getBus().post(new ResolveLocationEvents.MapAddressSelectedEvent(mAddressMarkerPosition));

        } else if (mSelectedBusStop != null) {
            getBus().post(new ResolveLocationEvents.MapBusStopSelectedEvent(mSelectedBusStop));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (ignoreFirstCameraChange) {
            Log.debug(TAG, "onCameraChange. Ignored");
            ignoreFirstCameraChange = false;
            return;
        }

        updateBusStopMarkers();
    }

    private void updateBusStopMarkers() {
        if (mMap == null || mMap.getCameraPosition().zoom < BUS_STOP_MIN_ZOOM) {
            return;
        }

        boolean taskRunning = isTaskRunning();
        Log.debug(TAG, "onCameraChange. Zoom valid. Task running: " + taskRunning);

        if (taskRunning) {
            cancelCurrentTask(false);
        }

        createTask(TASK_GET_BUS_STOPS, getBusStopsTaskProvider.get())
                .parameters(mMap.getProjection().getVisibleRegion().latLngBounds)
                .start(ResolveLocationMapFragment.this);
    }

    private void clearSelectedBusStop() {
        if (mSelectedBusStop != null) {
            replaceBusStopMarker(mSelectedBusStop.getId(), busUnselectedBitmapDescriptor);

            mSelectedBusStop = null;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        clearSelectedBusStop();

        String unSelectedMarkerId = marker.getId();
        Long newlySelectedBusStopId = mMarkerBusStopRelation.get(unSelectedMarkerId);

        if (newlySelectedBusStopId == null) {
            Log.warn(TAG, "mSelectedBusStopId not found.");
            return true;
        }

        clearAddressMarker();
        selectBusStopMarker(newlySelectedBusStopId, true);

        return true;
    }

    private void selectBusStopMarker(long busStopId, boolean moveCamera) {
        Marker newMarker = replaceBusStopMarker(busStopId, busSelectedBitmapDescriptor);
        if (newMarker == null) {
            return;
        }

        newMarker.showInfoWindow();

        boolean isBusStopAlreadySelected = (mSelectedBusStop != null);
        showContinueButton();

        BusStopContent busStopContent = mVisibleBusStops.get(busStopId);
        mSelectedBusStop = busStopContent.busStop;

        if (isBusStopAlreadySelected) {
            busStopInfoView.setVisibility(View.VISIBLE);
            busStopInfoView.show(mSelectedBusStop, false);
        }

        if (!moveCamera) {
            return;
        }

        Log.debug(TAG, "Animating camera to bus stop marker");

        GoogleMap.CancelableCallback openActivityCallback = null;
        boolean animateCamera = false;

        if (!isBusStopAlreadySelected) {
            // Only animate if this activity is in the foreground.
            animateCamera = true;

            openActivityCallback = new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    busStopInfoView.setVisibility(View.VISIBLE);
                    busStopInfoView.show(mSelectedBusStop, true);
                }

                @Override
                public void onCancel() {

                }
            };
        }

        // We need to calculate the conversion between pixels and latitude.
        float zoomLevel = mMap.getCameraPosition().zoom;
        if (latitudeLengthZoomLevel != zoomLevel) {
            LatLng latLng1 = mMap.getProjection().fromScreenLocation(new Point(0, 0));
            LatLng latLng2 = mMap.getProjection().fromScreenLocation(new Point(0, 1));

            latitudeLengthForPixel = Math.abs(latLng1.latitude - latLng2.latitude);
            latitudeLengthZoomLevel = zoomLevel;
        }

        LatLng markerOffsetPosition = new LatLng(
                mSelectedBusStop.getLatitude() - calculateBusStopLatitudeOffset(),
                mSelectedBusStop.getLongitude());

        moveCameraToPosition(markerOffsetPosition, animateCamera, true, openActivityCallback);
    }

    private double calculateBusStopLatitudeOffset() {
        // Determine the percentage of the screen used by the prompt.
        double promptHeightPx = getResources().getDimensionPixelSize(R.dimen.bus_stop_prompt_height);

        return ((promptHeightPx / 4) * latitudeLengthForPixel);
    }

    private void moveCameraToPosition(LatLng target, boolean animate, boolean ignoreCameraUpdate, final GoogleMap.CancelableCallback callback) {
        ignoreFirstCameraChange = ignoreCameraUpdate;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(mMap.getCameraPosition().zoom)
                .build();

        GoogleMap.CancelableCallback actualCallback = null;
        if (callback != null) {
            actualCallback = new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    callback.onFinish();
                }

                @Override
                public void onCancel() {
                    ignoreFirstCameraChange = false;
                    callback.onCancel();
                }
            };
        }

        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        if (animate) {
            mMap.animateCamera(update, 200, actualCallback);
        } else {
            mMap.moveCamera(update);
        }
    }

    private Marker replaceBusStopMarker(long busStopId, BitmapDescriptor bitmapDescriptor) {
        BusStopContent busStopContent = mVisibleBusStops.get(busStopId);
        if (busStopContent == null) {
            return null;
        }

        Marker oldMarker = busStopContent.marker;

        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(oldMarker.getPosition().latitude, oldMarker.getPosition().longitude))
                .icon(bitmapDescriptor)
        );
        mMarkerBusStopRelation.remove(oldMarker.getId());
        mMarkerBusStopRelation.put(newMarker.getId(), busStopId);

        busStopContent.marker = newMarker;

        oldMarker.remove();

        return newMarker;
    }

    public boolean handleBackPressed() {
        if (busStopInfoView.getVisibility() == View.VISIBLE) {
            busStopInfoView.dismiss();
            return true;
        }
        return false;
    }

    private static class BusStopContent {
        final BusStop busStop;
        Marker marker;

        public BusStopContent(BusStop busStop, Marker marker) {
            this.busStop = busStop;
            this.marker = marker;
        }
    }

    public static class HiddenMapWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private WeakReference<Activity> activityRef = null;

        public HiddenMapWindowAdapter(Activity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        // Hack to prevent info window from displaying: use a 0dp/0dp frame
        @SuppressLint("InflateParams")
        @Override
        public View getInfoWindow(Marker marker) {
            Activity activity = activityRef.get();
            if (activity == null) {
                return null;
            }
            return activity.getLayoutInflater().inflate(R.layout.no_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}