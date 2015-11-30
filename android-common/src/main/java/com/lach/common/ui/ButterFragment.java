package com.lach.common.ui;

import android.os.Bundle;
import android.view.View;

import com.lach.common.BaseApplication;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

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
