package com.lach.translink.ui.search;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.lach.common.BaseApplication;
import com.lach.common.ui.BaseActivity;
import com.lach.common.ui.view.Debouncer;
import com.lach.common.util.ClipboardUtil;
import com.lach.common.util.DialogUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.BR;
import com.lach.translink.ui.BaseViewModel;
import com.lach.translink.ui.PrimaryViewModel;
import com.lach.translink.ui.SharedEvents;
import com.lach.translink.ui.resolve.ResolveLocationActivity;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.ui.search.dialog.SavedLocationsDialog;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class SearchPlaceViewModel extends BaseViewModel {
    private final int REQUEST_LOCATION_ID = 101;

    private final PlaceType placeType;
    String address;

    @Inject
    LocationFavouriteDao locationFavouriteDao;

    public SearchPlaceViewModel(SearchViewModel parent, PlaceType placeType) {
        super(parent);
        this.placeType = placeType;
    }

    private String getBundleKey() {
        return "PlaceKey-" + placeType.name();
    }

    public PrimaryViewModel getParent() {
        return (PrimaryViewModel) super.getParent();
    }

    private BaseActivity getActivity() {
        return (BaseActivity) getParent().getActivity();
    }

    private Fragment getFragment() {
        return getParent().getFragment();
    }

    @Override
    public void init(Bundle savedInstanceState) {
        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        if (savedInstanceState != null) {
            this.address = savedInstanceState.getString(getBundleKey());
        }
        super.init(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseApplication.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseApplication.getEventBus().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(getBundleKey(), address);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_LOCATION_ID:
                    String address = data.getStringExtra(ResolveLocationActivity.ADDRESS_KEY);
                    PlaceType placeType = (PlaceType) data.getSerializableExtra(ResolveLocationActivity.PLACE_TYPE_KEY);
                    boolean favourite = data.getBooleanExtra(ResolveLocationActivity.IS_FAVOURITE_KEY, false);

                    // Ensure this result belongs to this particular place type.
                    if (this.placeType != placeType) {
                        return false;
                    }

                    changeLocation(address);

                    // Add the location to the favourites.
                    if (favourite && address != null) {
                        locationFavouriteDao.save(address);
                    }

                    return true;
            }
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }

    @Bindable
    public String getLabel() {
        return placeType.getDescription();
    }

    @Bindable
    public String getLocationDescription() {
        return address;
    }

    public View.OnClickListener getLabelClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity activity = getActivity();
                if (activity == null) {
                    return;
                }

                // It's strange that a state loss can occur during an onClick event, but it did happen.
                if (activity.isFragmentCommitAllowed()) {
                    if (locationFavouriteDao.getRowCount() > 0) {
                        SavedLocationsDialog dialog = SavedLocationsDialog.newInstance(placeType);
                        dialog.show(activity.getSupportFragmentManager(), "dialog");

                    } else {
                        DialogUtil.showAlertDialog(activity, "You have no favourite locations.", "No Favourites");
                    }
                }
            }
        };
    }

    public View.OnLongClickListener getLabelLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final CharSequence[] items = {"Copy Text", "Clear Location", "Add to Favourites"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Location Menu");

                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                if (address != null) {
                                    ClipboardUtil.setClipboardText(getActivity(), address);
                                }
                                break;

                            case 1:
                                changeLocation(null);
                                break;

                            case 2:
                                if (address != null) {
                                    locationFavouriteDao.save(address);
                                }
                                break;
                        }

                    }
                });
                builder.setNegativeButton("Cancel", null);

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        };
    }

    public View.OnClickListener getResolveClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentActivity activity = getActivity();
                Intent resolveLocationIntent = ResolveLocationActivity.createIntent(activity, placeType);

                getFragment().startActivityForResult(resolveLocationIntent, REQUEST_LOCATION_ID);
                activity.overridePendingTransition(0, 0);
            }
        };
    }

    public Debouncer getDebouncer() {
        return ((SearchViewModel) getParent()).getDebouncer();
    }

    public void changeLocation(String newAddress) {
        address = newAddress;
        notifyPropertyChanged(BR.locationDescription);
    }

    public void clearLocation() {
        address = null;
        notifyPropertyChanged(BR.locationDescription);
    }

    @Subscribe
    public void onSavedLocationSelected(SharedEvents.SavedLocationSelectedEvent event) {
        String address = event.getAddress();
        if (address != null && event.getPlaceType() == placeType) {
            changeLocation(address);
        }
    }
}