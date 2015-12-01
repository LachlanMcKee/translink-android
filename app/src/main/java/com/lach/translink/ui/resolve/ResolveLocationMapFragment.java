package com.lach.translink.ui.resolve;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lach.common.BaseApplication;
import com.lach.common.data.preference.BooleanPreference;
import com.lach.common.data.preference.DoublePreference;
import com.lach.common.data.preference.FloatPreference;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.log.Log;
import com.lach.common.ui.view.ScaleAnimator;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.ui.UiPreference;
import com.lach.translink.data.location.PlaceType;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveLocationMapFragment extends Fragment implements GoogleMap.OnMapClickListener {
    private static final String TAG = "ResolveLocationMapFragment";

    private static final String PLACE_TYPE = "place_type";

    private static final BooleanPreference PREF_POSITION_SET = new BooleanPreference("position_set", false);
    private static final DoublePreference PREF_LAT = new DoublePreference("lat", 0.0d);
    private static final DoublePreference PREF_LONG = new DoublePreference("long", 0.0d);
    private static final FloatPreference PREF_ZOOM = new FloatPreference("zoom", 0.0f);
    private static final FloatPreference PREF_BEARING = new FloatPreference("bearing", 0.0f);

    private static final String BUNDLE_CURRENT_MARKER_POSITION = "current_marker_position";

    private static final CameraPosition BRISBANE =
            new CameraPosition.Builder().target(new LatLng(-27.4679400, 153.0280900))
                    .zoom(13.0f)
                    .bearing(0)
                    .build();

    private GoogleMap mMap;
    private LatLng mCurrentMarkerPosition;

    @Inject
    PreferencesProvider preferencesProvider;

    @InjectView(R.id.resolve_map_coordinator)
    ViewGroup coordinatorLayout;

    @InjectView(R.id.resolve_map_title)
    TextView title;

    @InjectView(R.id.resolve_map_subtitle)
    TextView subtitle;

    @InjectView(R.id.resolve_map_continue)
    View continueButton;

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
        application.getCoreComponent().inject(this);

        ButterKnife.inject(this, view);

        mapView = (MapView) view.findViewById(R.id.resolve_map);
        mapView.onCreate(savedInstanceState);

        try {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

            int buttonMargin = getResources().getDimensionPixelOffset(R.dimen.map_location_button_margin);

            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, buttonMargin, buttonMargin);

        } catch (Exception ignored) {
            // If we fail to move the button it isn't the end of the world.
            Log.warn(TAG, "Unable to move the myLocation button.");
        }

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

        // Reload the current marker position if it exists.
        if (savedInstanceState != null) {
            mCurrentMarkerPosition = savedInstanceState.getParcelable(BUNDLE_CURRENT_MARKER_POSITION);
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                initMapSettings(googleMap);

                if (mCurrentMarkerPosition != null) {
                    addMarker(mCurrentMarkerPosition);
                }
            }
        });
    }

    protected Bus getBus() {
        return BaseApplication.getEventBus();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register ourselves so that we can provide the initial value.
        getBus().register(this);
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Always unregister when an object no longer should be on the bus.
        getBus().unregister(this);
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
        outState.putParcelable(BUNDLE_CURRENT_MARKER_POSITION, mCurrentMarkerPosition);

        super.onSaveInstanceState(outState);
    }

    private void initMapSettings(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity());

        mMap = googleMap;

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMapClickListener(this);

        CameraPosition cameraPosition;

        Preferences preferences = preferencesProvider.getPreferences();
        boolean lastPositionSet = PREF_POSITION_SET.get(preferences);

        if (lastPositionSet) {
            double latitude = PREF_LAT.get(preferences);
            double longitude = PREF_LONG.get(preferences);
            float zoom = PREF_ZOOM.get(preferences);
            float bearing = PREF_BEARING.get(preferences);

            cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude))
                    .zoom(zoom)
                    .bearing(bearing)
                    .build();
        } else {
            cameraPosition = BRISBANE;
        }

        if (!googleMap.getCameraPosition().equals(cameraPosition)) {
            changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void changeCamera(CameraUpdate update, boolean animate) {
        if (animate) {
            mMap.animateCamera(update);
        } else {
            mMap.moveCamera(update);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarker(latLng);
    }

    private void addMarker(LatLng latLng) {
        mMap.clear();

        boolean hasExistingMarker = (mCurrentMarkerPosition != null);
        mCurrentMarkerPosition = latLng;

        mMap.addMarker(new MarkerOptions().position(latLng));

        showContinueButton(!hasExistingMarker);
    }

    private void showContinueButton(boolean animate) {
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
    public void onDestroy() {
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
    void confirmMarker() {
        if (mCurrentMarkerPosition == null) {
            return;
        }

        getBus().post(new ResolveLocationEvents.MapMarkerSelectedEvent(mCurrentMarkerPosition));
    }

}