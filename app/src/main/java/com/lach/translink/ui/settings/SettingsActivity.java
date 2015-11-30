package com.lach.translink.ui.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.ui.BaseActivity;
import com.lach.translink.activities.R;

public class SettingsActivity extends BaseActivity {
    private static final String SETTINGS_FRAGMENT_ID = "SettingsFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_single_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment detectionsContent = fm.findFragmentById(R.id.fragment_content);

        if (fm.findFragmentByTag(SETTINGS_FRAGMENT_ID) != null) {
            // Ensure the fragment is not recreated.
            return;
        }

        SettingsFragment settingsFragment = SettingsFragment.newInstance();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction ft = fm.beginTransaction();
        if (detectionsContent == null) {
            ft.add(R.id.fragment_content, settingsFragment, SETTINGS_FRAGMENT_ID);
        } else {
            ft.replace(R.id.fragment_content, settingsFragment, SETTINGS_FRAGMENT_ID);
        }

        ft.commit();
    }

}
