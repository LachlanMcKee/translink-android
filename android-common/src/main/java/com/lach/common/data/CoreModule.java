package com.lach.common.data;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class CoreModule {
    private Application application;

    public CoreModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationContext
    Context providesApplicationContext() {
        return application;
    }

}
