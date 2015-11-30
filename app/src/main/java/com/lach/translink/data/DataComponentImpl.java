package com.lach.translink.data;

import com.lach.common.data.provider.ProviderHelperModule;
import com.lach.translink.data.DataComponent;
import com.lach.translink.data.journey.JourneyDataModule;
import com.lach.translink.data.location.LocationDataModule;

import dagger.Component;

@Component(modules = {LocationDataModule.class, JourneyDataModule.class, ProviderHelperModule.class})
public interface DataComponentImpl extends DataComponent {

}
