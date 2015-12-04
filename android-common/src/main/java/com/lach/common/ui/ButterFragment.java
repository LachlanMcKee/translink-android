package com.lach.common.ui;

import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ButterFragment extends android.support.v4.app.Fragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    protected EventBus getBus() {
        return EventBus.getDefault();
    }

}
