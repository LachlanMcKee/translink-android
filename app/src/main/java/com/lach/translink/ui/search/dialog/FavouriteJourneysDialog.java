package com.lach.translink.ui.search.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavourite;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;

import java.util.List;

import javax.inject.Inject;

public class FavouriteJourneysDialog extends DialogFragment {

    public static final String INTENT_JOURNEY_CRITERIA_KEY = "location";

    @Inject
    JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;

    public static FavouriteJourneysDialog newInstance() {
        return new FavouriteJourneysDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        b.setTitle("Select a saved Journey");

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        final List<? extends JourneyCriteriaFavourite> favouriteList = journeyCriteriaFavouriteDao.getAllRowsAsItems();

        JourneyAdapter adapter = new JourneyAdapter(getActivity(), favouriteList, false);
        b.setAdapter(adapter,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        JourneyCriteriaFavourite favourite = favouriteList.get(item);

                        Intent data = new Intent();
                        data.putExtra(INTENT_JOURNEY_CRITERIA_KEY, favourite.getJourneyCriteria());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                    }

                });

        b.setCancelable(true);

        AlertDialog dialog = b.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}