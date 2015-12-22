package com.lach.common.data;

import com.lach.common.async.AsyncTaskFragment;

import dagger.Component;

@Component(modules = CoreModule.class)
public interface CoreComponent {

    void inject(AsyncTaskFragment.DialogTaskFragment inject);

    void inject(AsyncTaskFragment.HiddenTaskFragment inject);

}
