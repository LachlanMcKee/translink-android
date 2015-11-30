package com.lach.common.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.lach.common.log.Log;
import com.lach.common.R;

import java.lang.reflect.Method;

public class EnhancedListPreference extends ListPreference {
    private static final String TAG = "EnhancedListPreference";

    private final EnhancedPreferenceHelper helper;

    private int mClickedDialogEntryIndex;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EnhancedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.v_enhanced_preference_standard);

        helper = new EnhancedPreferenceHelper();
        helper.init(this, attrs, defStyleAttr);
    }

    public EnhancedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.v_enhanced_preference_standard);

        helper = new EnhancedPreferenceHelper();
        helper.init(this, attrs, 0);
    }

    public EnhancedListPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        helper.bindView(this, view);
    }

    @Override
    protected void showDialog(Bundle state) {
        Context context = getContext();

        android.support.v7.app.AlertDialog.Builder mSupportBuilder = new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(getDialogTitle())
                .setIcon(getDialogIcon())
                .setPositiveButton(getPositiveButtonText(), this)
                .setNegativeButton(getNegativeButtonText(), this);

        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            mSupportBuilder.setView(contentView);
        } else {
            mSupportBuilder.setMessage(getDialogMessage());
        }

        onPrepareDialogBuilder(mSupportBuilder);

        try {
            Method destroyListener = PreferenceManager.class.getDeclaredMethod("registerOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            destroyListener.setAccessible(true);
            destroyListener.invoke(getPreferenceManager(), this);
        } catch (Exception ex) {
            Log.error(TAG, "Failed to register", ex);
        }

        // Create the dialog
        final AlertDialog dialog = mSupportBuilder.create();
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        dialog.setOnDismissListener(this);
        onDialogCreated(dialog);
        dialog.show();
    }

    protected void onDialogCreated(AlertDialog dialog) {

    }

    void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedDialogEntryIndex = which;

                        /*
                         * Clicking on an item simulates the positive button
                         * click, and dismisses the dialog.
                         */
                        EnhancedListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });

        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && mClickedDialogEntryIndex >= 0 && entryValues != null) {
            String value = entryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
                setSummary(getEntries()[mClickedDialogEntryIndex]);
            }
        }
    }

    @Override
    protected View onCreateDialogView() {
        int dialogLayoutResource = getDialogLayoutResource();
        if (getDialogLayoutResource() == 0) {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        return inflater.inflate(dialogLayoutResource, null);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        updateSummary(value);
    }

    protected void updateSummary(String value) {
        if (value != null) {
            setSummary(getEntries()[findIndexOfValue(value)]);
        }
    }

}
