package com.lach.common.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lach.common.R;

public abstract class SingleFragmentActivity<FRAGMENT_TYPE extends Fragment> extends BaseActivity {
    private static final String TAG_FRAGMENT = "single_fragment";

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_single_fragment);

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_FRAGMENT) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_content, createFragment(), TAG_FRAGMENT);
            ft.commit();
        }
    }

    public abstract FRAGMENT_TYPE createFragment();
}
