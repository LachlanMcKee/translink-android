package com.lach.translink.ui.impl.history;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.journey.history.JourneyCriteriaHistory;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.data.place.PlaceParser;

import java.util.List;

import javax.inject.Inject;

public class HistoryDialog extends DialogFragment {

    public static final String INTENT_JOURNEY_CRITERIA_KEY = "journeySearch";
    private static final String BUNDLE_IGNORE_HISTORY_TIME = "ignoreHistoryTime";

    @Inject
    JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    @Inject
    PlaceParser placeParser;

    public static HistoryDialog newInstance(boolean ignoreHistoryTime) {
        HistoryDialog dialog = new HistoryDialog();

        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_IGNORE_HISTORY_TIME, ignoreHistoryTime);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle("Journey History");
        b.setCancelable(true);

        boolean ignoreHistoryTime = getArguments().getBoolean(BUNDLE_IGNORE_HISTORY_TIME);

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        final List<? extends JourneyCriteriaHistory> criteriaHistory = journeyCriteriaHistoryDao.getAllRowsAsItems();
        b.setAdapter(new HistoryArrayAdapter(getActivity(), placeParser, criteriaHistory, ignoreHistoryTime),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        JourneyCriteriaHistory criteria = criteriaHistory.get(item);

                        Intent data = new Intent();
                        data.putExtra(INTENT_JOURNEY_CRITERIA_KEY, criteria.getJourneyCriteria());

                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                    }

                });

        AlertDialog dialog = b.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}
