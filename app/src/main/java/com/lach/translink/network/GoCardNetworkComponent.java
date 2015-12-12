package com.lach.translink.network;

import com.lach.translink.ui.impl.gocard.GoCardDetailsDialog;
import com.lach.translink.ui.impl.gocard.GoCardInfoFragment;

public interface GoCardNetworkComponent {
    void injectFragment(GoCardInfoFragment inject);

    void injectFragment(GoCardInfoFragment.GoCardGraphFragment inject);

    void inject(GoCardDetailsDialog inject);
}
