package com.lach.translink.network.gocard;

import com.lach.translink.ui.impl.gocard.GoCardDetailsDialog;
import com.lach.translink.ui.impl.gocard.GoCardInfoFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {GoCardNetworkModule.class})
public interface GoCardNetworkComponent {
    void injectFragment(GoCardInfoFragment inject);

    void injectFragment(GoCardInfoFragment.GoCardGraphFragment inject);

    void inject(GoCardDetailsDialog inject);
}
