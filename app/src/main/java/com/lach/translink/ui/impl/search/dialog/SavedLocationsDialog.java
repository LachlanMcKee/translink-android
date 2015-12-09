package com.lach.translink.ui.impl.search.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.lach.common.ui.dialog.ButterCustomDialogFragment;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.data.location.favourite.LocationFavourite;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.ui.impl.SharedEvents;

import java.util.List;

import javax.inject.Inject;

public class SavedLocationsDialog extends ButterCustomDialogFragment {

    private static final String BUNDLE_PLACE_TYPE_KEY = "place_type";

    @Inject
    LocationFavouriteDao locationFavouriteDao;

    @Inject
    PlaceParser placeParser;

    public static SavedLocationsDialog newInstance(PlaceType placeType) {
        SavedLocationsDialog dialog = new SavedLocationsDialog();

        Bundle b = new Bundle(1);
        b.putSerializable(BUNDLE_PLACE_TYPE_KEY, placeType);
        dialog.setArguments(b);

        return dialog;
    }

    @Override
    public AlertDialog.Builder getDialogBuilder(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        final List<? extends LocationFavourite> addressList = locationFavouriteDao.getAllRowsAsItems();
        CharSequence[] items = new CharSequence[addressList.size()];

        for (int i = 0; i < addressList.size(); i++) {
            items[i] = placeParser.prettyPrintPlace(addressList.get(i).getAddress());
        }

        b.setTitle("Pick a location");
        b.setCancelable(true);

        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                LocationFavourite location = addressList.get(item);
                PlaceType placeType = (PlaceType) getArguments().getSerializable(BUNDLE_PLACE_TYPE_KEY);

                getBus().post(new SharedEvents.SavedLocationSelectedEvent(location.getAddress(), placeType));
            }
        });

        setCancelable(true);
        return b;
    }

    @Override
    public boolean isCanceledOnTouchOutside() {
        return true;
    }

    @Override
    public View getDialogView(LayoutInflater inflater) {
        return null;
    }

    @Override
    public void onDialogInjected(Bundle savedInstanceState) {

    }

}
