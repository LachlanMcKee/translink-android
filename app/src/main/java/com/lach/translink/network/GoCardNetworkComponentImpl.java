package com.lach.translink.network;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {GoCardNetworkModule.class})
public interface GoCardNetworkComponentImpl extends GoCardNetworkComponent {

}
