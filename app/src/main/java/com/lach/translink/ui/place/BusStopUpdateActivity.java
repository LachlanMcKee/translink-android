package com.lach.translink.ui.place;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.ui.BaseActivity;
import com.lach.translink.activities.R;

public class BusStopUpdateActivity extends BaseActivity {
    private static final String TAG_INSTALL = "install_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_single_fragment);

        FragmentManager fm = getSupportFragmentManager();
        BusStopUpdateFragment listFragment = (BusStopUpdateFragment) fm.findFragmentByTag(TAG_INSTALL);

        if (listFragment == null) {
            listFragment = BusStopUpdateFragment.newInstance();

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, listFragment, TAG_INSTALL);
            ft.commit();
        }
    }
}
