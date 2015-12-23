package com.lach.translink.ui.impl.resolve;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.data.map.MapBounds;
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
import com.lach.common.data.map.MapMarker;
import com.lach.common.data.map.MapPosition;
import com.lach.translink.ui.presenter.resolve.ResolveLocationMapPresenter;
import com.lach.translink.ui.presenter.resolve.ResolveLocationMapPresenterImpl;
import com.lach.translink.ui.view.resolve.ResolveLocationMapView;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveLocationMapFragment extends AsyncTaskFragment implements ResolveLocationMapView<Marker>,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "ResolveLocationMapFragment";

    private static final String PLACE_TYPE = "place_type";

    private static final BooleanPreference PREF_POSITION_SET = new BooleanPreference("position_set", false);
    private static final DoublePreference PREF_LAT = new DoublePreference("lat", 0.0d);
    private static final DoublePreference PREF_LONG = new DoublePreference("long", 0.0d);
    private static final FloatPreference PREF_ZOOM = new FloatPreference("zoom", 0.0f);
    private static final FloatPreference PREF_BEARING = new FloatPreference("bearing", 0.0f);

    private static final String BUNDLE_LATITUDE_LENGTH_FOR_PIXEL = "latitude_length_for_pixel";
    private static final String BUNDLE_LATITUDE_LENGTH_ZOOM = "latitude_length_zoom";

    private static final CameraPosition BRISBANE =
            new CameraPosition.Builder().target(new LatLng(-27.4679400, 153.0280900))
                    .zoom(13.0f)
                    .bearing(0)
                    .build();

    private BitmapDescriptor busSelectedBitmapDescriptor;
    private BitmapDescriptor busUnselectedBitmapDescriptor;

    private GoogleMap mMap;
    private float latitudeLengthZoomLevel = 0.0f;
    private double latitudeLengthForPixel = 0.0d;
    private boolean ignoreNextCameraChangeEvent;

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    ResolveLocationMapPresenter mPresenter;

    @InjectView(R.id.resolve_title)
    TextView title;

    @InjectView(R.id.resolve_subtitle)
    TextView subtitle;

    @InjectView(R.id.resolve_map_continue)
    View continueButton;

    @InjectView(R.id.resolve_map_bus_stop_info)
    ResolveBusStopInfoView busStopInfoView;

    // Don't data-bind this. We need to reference it after onDestroyView.
    private MapView mapView;

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

        mPresenter.setView(this);
        mPresenter.onCreate(savedInstanceState);

        // Reload the current marker position if it exists.
        if (savedInstanceState != null) {
            latitudeLengthForPixel = savedInstanceState.getDouble(BUNDLE_LATITUDE_LENGTH_FOR_PIXEL, 0);
            latitudeLengthZoomLevel = savedInstanceState.getFloat(BUNDLE_LATITUDE_LENGTH_ZOOM, 0);
        }

        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.resolve_map_container);

        // Configure the map.
        mapView = new MapView(getActivity(), createGoogleMapOptions());

        mapViewContainer.addView(mapView);
        mapView.onCreate(savedInstanceState);
        MapUtil.moveLocationButtonToBottomRight(mapView, getResources().getDimensionPixelOffset(R.dimen.map_location_button_margin));

        // Set the appropriate title.
        PlaceType placeType = (PlaceType) getArguments().getSerializable(PLACE_TYPE);
        String description;
        if (placeType != null) {
            description = placeType.getDescription().toLowerCase();
        } else {
            description = "location";
        }
        title.setText(getString(R.string.resolve_title, description));

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                initMapSettings(googleMap);
                mPresenter.onMapInit();
            }
        });

        busStopInfoView.setDismissListener(new ResolveBusStopInfoView.DismissListener() {
            @Override
            public void onDismissCompleted() {
                mPresenter.onBusStopDismissed();
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

        mPresenter.onDestroy();

        ButterKnife.reset(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        mPresenter.onSaveInstanceState(outState);

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

            LatLng latLng;
            MapPosition mapPosition = mPresenter.getPersistedMapPosition();
            if (mapPosition != null) {
                latLng = convertMapPositionToLatLng(mapPosition);
            } else {
                latLng = new LatLng(latitude, longitude);
            }

            float zoom = PREF_ZOOM.get(preferences);
            float bearing = PREF_BEARING.get(preferences);

            cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
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
        mPresenter.onMapClick(convertLatLngToMapPosition(latLng));
    }

    @Override
    public void showContinueButton(boolean animate) {
        if (!animate) {
            continueButton.setVisibility(View.VISIBLE);
        }

        if (animate) {
            ScaleAnimator continueButtonScaler = new ScaleAnimator(continueButton);
            continueButtonScaler.show();
        }
        subtitle.setText(R.string.resolve_map_subtitle_continue);
    }

    @Override
    public MapMarker<Marker> addMarker(MapPosition mapPosition) {
        return wrapMarker(mMap.addMarker(new MarkerOptions()
                .position(convertMapPositionToLatLng(mapPosition))));
    }

    @Override
    public void hideContinueButton() {
        continueButton.setVisibility(View.GONE);
        subtitle.setText(R.string.resolve_map_subtitle_intro);
    }

    @Override
    public MapMarker<Marker> addBusStopMarker(MapPosition mapPosition, boolean isSelected) {
        return wrapMarker(mMap.addMarker(new MarkerOptions()
                .position(convertMapPositionToLatLng(mapPosition))
                .icon(isSelected ? busSelectedBitmapDescriptor : busUnselectedBitmapDescriptor)));
    }

    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        mPresenter.onTaskFinished(taskId, result);
    }

    @Override
    public void onTaskCancelled(int taskId) {
        mPresenter.onTaskCancelled(taskId);
    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        return mPresenter.onTaskError(taskId, errorId);
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
        mPresenter.onConfirmed();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (ignoreNextCameraChangeEvent) {
            Log.debug(TAG, "onCameraChange. Ignored");
            ignoreNextCameraChangeEvent = false;
            return;
        }

        mPresenter.onCameraChange();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mPresenter.onMarkerClick(marker.getId());
        return true;
    }

    @Override
    public double calculateBusStopLatitudeOffset() {
        // Determine the percentage of the screen used by the prompt.
        double promptHeightPx = getResources().getDimensionPixelSize(R.dimen.bus_stop_prompt_height);

        return ((promptHeightPx / 4) * latitudeLengthForPixel);
    }

    @Override
    public void showBusStopDetails(BusStop busStop, boolean animate) {
        busStopInfoView.setVisibility(View.VISIBLE);
        busStopInfoView.show(busStop, animate);
    }

    @Override
    public void moveCameraToBusStop(BusStop busStop, boolean animateCamera, final ResolveLocationMapPresenterImpl.MoveCameraListener listener) {
        // We need to calculate the conversion between pixels and latitude.
        float zoomLevel = getMapZoomLevel();
        if (latitudeLengthZoomLevel != zoomLevel) {
            LatLng latLng1 = mMap.getProjection().fromScreenLocation(new Point(0, 0));
            LatLng latLng2 = mMap.getProjection().fromScreenLocation(new Point(0, 1));

            latitudeLengthForPixel = Math.abs(latLng1.latitude - latLng2.latitude);
            latitudeLengthZoomLevel = zoomLevel;
        }

        LatLng markerOffsetPosition = new LatLng(
                busStop.getLatitude() - calculateBusStopLatitudeOffset(),
                busStop.getLongitude());

        moveCameraToPosition(markerOffsetPosition, animateCamera, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                listener.onFinish();
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        });
    }

    @Override
    public void removeMapMarker(MapMarker<Marker> marker) {
        marker.getMarker().remove();
    }

    @Override
    public void bringMarkerToFront(MapMarker<Marker> marker) {
        marker.getMarker().showInfoWindow();
    }

    private void moveCameraToPosition(LatLng target, boolean animate, final GoogleMap.CancelableCallback callback) {
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
                    ignoreNextCameraChangeEvent = false;
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

    public boolean handleBackPressed() {
        if (busStopInfoView.getVisibility() == View.VISIBLE) {
            busStopInfoView.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public boolean isUiReady() {
        return isAdded();
    }

    @Override
    public boolean isUiVisible() {
        return isVisible();
    }

    @Override
    public boolean isMapReady() {
        return mMap != null;
    }

    @Override
    public float getMapZoomLevel() {
        return mMap.getCameraPosition().zoom;
    }

    @Override
    public MapBounds getMapBounds() {
        LatLngBounds googleMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        // Convert this into a Java POJO, removing the maps library dependency.
        return new MapBounds(
                convertLatLngToMapPosition(googleMapBounds.southwest),
                convertLatLngToMapPosition(googleMapBounds.northeast)
        );
    }

    private LatLng convertMapPositionToLatLng(@NonNull MapPosition mapPosition) {
        return new LatLng(mapPosition.getLatitude(), mapPosition.getLongitude());
    }

    private MapPosition convertLatLngToMapPosition(@NonNull LatLng latLng) {
        return new MapPosition(latLng.latitude, latLng.longitude);
    }

    private MapMarker<Marker> wrapMarker(final Marker marker) {
        return new MapMarker<Marker>() {
            @Override
            public String getId() {
                return marker.getId();
            }

            @Override
            public MapPosition getMapPosition() {
                return convertLatLngToMapPosition(marker.getPosition());
            }

            @Override
            public Marker getMarker() {
                return marker;
            }
        };
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