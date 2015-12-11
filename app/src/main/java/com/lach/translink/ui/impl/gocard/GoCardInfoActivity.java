package com.lach.translink.ui.impl.gocard;

import com.lach.common.ui.SingleFragmentActivity;

public class GoCardInfoActivity extends SingleFragmentActivity<GoCardInfoFragment> {

    @Override
    public GoCardInfoFragment createFragment() {
        return GoCardInfoFragment.newInstance();
    }

}
