package com.lach.translink.activities.data;

import android.content.Context;
import android.preference.PreferenceManager;

import com.lach.common.data.ApplicationContext;
import com.lach.common.data.preference.AndroidPreferences;
import com.lach.common.data.preference.InMemoryPreferences;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockCoreModule {
    InMemoryPreferences inMemoryPreferences;

    public MockCoreModule() {
        inMemoryPreferences = new InMemoryPreferences();
    }

    @Provides
    @ApplicationContext
    Context providesApplicationContext() {
        return null;
    }

    @Singleton
    @Provides
    PreferencesProvider providesPreferencesProvider() {
        return new PreferencesProvider() {
            @Override
            public Preferences getPreferences() {
                return inMemoryPreferences;
            }
        };
    }

}
