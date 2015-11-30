package com.lach.common.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.lach.common.BaseApplication;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

public abstract class ButterCustomDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = getDialogView(inflater);

        ButterKnife.inject(this, dialogView);
        onDialogInjected(savedInstanceState);

        AlertDialog dialog;
        if (builder != null) {
            builder.setView(dialogView);
            dialog = builder.create();

        } else {
            dialog = new AccessibleAlertDialog(getActivity());
            dialog.setView(dialogView);
        }
        onDialogCreated(savedInstanceState, dialog);
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside());
        return dialog;
    }

    @SuppressWarnings("UnusedParameters")
    protected AlertDialog.Builder getDialogBuilder(Bundle savedInstanceState) {
        return null;
    }

    @SuppressWarnings("UnusedParameters")
    protected void onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {

    }

    protected abstract View getDialogView(LayoutInflater inflater);

    @SuppressWarnings("UnusedParameters")
    protected abstract void onDialogInjected(Bundle savedInstanceState);

    protected boolean isCanceledOnTouchOutside() {
        return false;
    }

    protected Bus getBus() {
        return BaseApplication.getEventBus();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register ourselves so that we can provide the initial value.
        getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Always unregister when an object no longer should be on the bus.
        getBus().unregister(this);
    }

}
