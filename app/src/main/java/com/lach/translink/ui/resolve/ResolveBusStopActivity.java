package com.lach.translink.ui.resolve;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.BaseApplication;
import com.lach.common.ui.BaseActivity;
import com.lach.translink.activities.R;
import com.lach.translink.data.place.bus.BusStop;

public class ResolveBusStopActivity extends BaseActivity {
    private static final String TAG_BUS_STOP = "bus_stop_fragment";
    private static final String BUNDLE_KEY_BUS_STOP = "bus_stop";

    public static Intent createIntent(Context context, BusStop busStop) {
        Intent intent = new Intent(context, ResolveBusStopActivity.class);
        intent.putExtra(BUNDLE_KEY_BUS_STOP, busStop);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_single_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ResolveBusStopFragment resolveBusStopFragment = (ResolveBusStopFragment) fm.findFragmentByTag(TAG_BUS_STOP);

        if (resolveBusStopFragment == null) {
            BusStop busStop = getIntent().getParcelableExtra(BUNDLE_KEY_BUS_STOP);
            resolveBusStopFragment = ResolveBusStopFragment.newInstance(busStop);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, resolveBusStopFragment, TAG_BUS_STOP);
            ft.commit();
        }
    }

    @Override
    public BaseApplication.ThemeType getThemeType() {
        return BaseApplication.ThemeType.NONE;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        ResolveBusStopFragment resolveBusStopFragment = (ResolveBusStopFragment) getSupportFragmentManager().findFragmentByTag(TAG_BUS_STOP);
        if (resolveBusStopFragment != null) {
            resolveBusStopFragment.dismiss();
        }
    }
}
