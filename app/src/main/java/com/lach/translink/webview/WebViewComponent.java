package com.lach.translink.webview;

import com.lach.common.data.CoreModule;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.journey.JourneyDataModule;
import com.lach.translink.data.place.PlaceDataModule;
import com.lach.translink.network.NetworkModule;
import com.lach.translink.ui.impl.result.JourneyResultFragment;
import com.lach.translink.tasks.result.TaskJourneySearch;

import dagger.Component;

@Component(modules = {WebViewModule.class, NetworkModule.class, JourneyDataModule.class, PlaceDataModule.class, CoreModule.class})
public interface WebViewComponent {

    void inject(TranslinkApplication inject);

    void inject(JourneyResultFragment inject);

    void inject(TaskJourneySearch inject);

}
