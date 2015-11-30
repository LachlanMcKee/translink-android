package com.lach.translink.ui.gocard;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.ui.BaseActivity;
import com.lach.translink.activities.R;

public class GoCardInfoActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_single_fragment);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, GoCardInfoFragment.newInstance());
            ft.commit();
        }
    }

}
