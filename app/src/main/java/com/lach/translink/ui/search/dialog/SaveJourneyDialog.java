package com.lach.translink.ui.search.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.lach.common.ui.dialog.ButterCustomDialogFragment;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavourite;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;

import java.util.Date;

import javax.inject.Inject;

import butterknife.InjectView;

public class SaveJourneyDialog extends ButterCustomDialogFragment {

    @Inject
    JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;

    private JourneyCriteria journeyCriteria;

    @InjectView(R.id.journey_name_text)
    EditText journeyNameEditText;

    @InjectView(R.id.save_journey_from_location)
    CheckBox fromLocationCheckBox;

    @InjectView(R.id.save_journey_to_location)
    CheckBox toLocationCheckBox;

    @InjectView(R.id.save_journey_leave_type)
    CheckBox leaveTypeCheckBox;

    @InjectView(R.id.save_journey_transport_type)
    CheckBox transportTypeCheckBox;

    @InjectView(R.id.save_journey_time)
    CheckBox timeCheckBoxCheckBox;

    public static SaveJourneyDialog newInstance(JourneyCriteria journeyCriteria) {
        SaveJourneyDialog dialog = new SaveJourneyDialog();

        Bundle args = new Bundle();
        args.putParcelable("journeyCriteria", journeyCriteria);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    protected void onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        dialog.setTitle("Save Favourite Trip");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (addJourney()) {
                            dismiss();
                        }
                    }
                }
        );
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );

    }

    @SuppressLint("InflateParams")
    @Override
    public View getDialogView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.d_save_journey_dialog, null);
    }

    @Override
    public void onDialogInjected(Bundle savedInstanceState) {
        journeyCriteria = getArguments().getParcelable("journeyCriteria");

        adjustCheckBoxPadding(fromLocationCheckBox);
        adjustCheckBoxPadding(toLocationCheckBox);
        adjustCheckBoxPadding(leaveTypeCheckBox);
        adjustCheckBoxPadding(transportTypeCheckBox);
        adjustCheckBoxPadding(timeCheckBoxCheckBox);

        if (journeyCriteria.getFromAddress() == null) {
            fromLocationCheckBox.setChecked(false);
            fromLocationCheckBox.setEnabled(false);
        }

        if (journeyCriteria.getToAddress() == null) {
            toLocationCheckBox.setChecked(false);
            toLocationCheckBox.setEnabled(false);
        }
    }

    private void adjustCheckBoxPadding(CheckBox checkBox) {
        final float scale = this.getResources().getDisplayMetrics().density;
        checkBox.setPadding(checkBox.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
                checkBox.getPaddingTop(),
                checkBox.getPaddingRight(),
                checkBox.getPaddingBottom());
    }

    private boolean addJourney() {
        if (journeyNameEditText.length() == 0) {
            Toast.makeText(getActivity(), "You must specify a name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        JourneyCriteria criteria = new JourneyCriteria();
        if (fromLocationCheckBox.isChecked()) {
            criteria.setFromAddress(journeyCriteria.getFromAddress());
        }

        if (toLocationCheckBox.isChecked()) {
            criteria.setToAddress(journeyCriteria.getToAddress());
        }

        if (leaveTypeCheckBox.isChecked()) {
            criteria.setJourneyTimeCriteria(journeyCriteria.getJourneyTimeCriteria());
        }

        if (transportTypeCheckBox.isChecked()) {
            criteria.setJourneyTransport(journeyCriteria.getJourneyTransport());
        }

        if (timeCheckBoxCheckBox.isChecked()) {
            criteria.setTime(journeyCriteria.getTime());
        }

        JourneyCriteriaFavourite favourite = journeyCriteriaFavouriteDao.createModel();
        favourite.setName(journeyNameEditText.getText().toString());
        favourite.setDateCreated(new Date());
        favourite.setJourneyCriteria(criteria);

        journeyCriteriaFavouriteDao.insertRows(favourite);

        Toast.makeText(getActivity(), "Journey added successfully.", Toast.LENGTH_SHORT).show();

        return true;

    }

}