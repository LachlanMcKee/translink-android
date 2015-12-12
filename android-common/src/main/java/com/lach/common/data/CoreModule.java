package com.lach.common.data;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.lach.common.data.preference.AndroidPreferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.data.preference.Preferences;

import dagger.Module;
import dagger.Provides;

@Module
public class CoreModule {
    private final Application application;

    public CoreModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationContext
    Context providesApplicationContext() {
        return application;
    }

    @Provides
    PreferencesProvider providesPreferencesProvider() {
        return new PreferencesProvider() {
            @Override
            public Preferences getPreferences() {
                return new AndroidPreferences(PreferenceManager.getDefaultSharedPreferences(application));
            }
        };
    }
}
