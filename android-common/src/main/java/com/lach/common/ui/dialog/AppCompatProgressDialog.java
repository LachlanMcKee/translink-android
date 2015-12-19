package com.lach.common.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.lach.common.R;

public class AppCompatProgressDialog extends AppCompatDialog {
    private TextView mMessageView;

    public AppCompatProgressDialog(Context context) {
        super(context);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();
    }

    @SuppressLint("InflateParams")
    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.d_progress, null);

        mMessageView = (TextView) view.findViewById(R.id.progress_dialog_message);

        setContentView(view);
    }

    public void setMessage(String message) {
        mMessageView.setText(message);
    }
}
