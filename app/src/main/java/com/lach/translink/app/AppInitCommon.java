package com.lach.translink.app;

import android.app.Application;

import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.BooleanPreference;
import com.lach.common.data.preference.Preferences;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.journey.JourneyPreference;
import com.lach.translink.ui.impl.UiPreference;
import com.lach.translink.util.DataResource;

public class AppInitCommon {
    private static final BooleanPreference PREFERENCES_LOADED = new BooleanPreference("preferences_loaded", false);

    /**
     * Should be called when the app is initially loaded.
     */
    public static void init(Application application) {
        TranslinkApplication translinkApplication = (TranslinkApplication) application;

        Preferences preferences =  translinkApplication.getPreferencesProvider().getPreferences();

        if (!PREFERENCES_LOADED.get(preferences)) {
            Preferences.Editor editor = preferences.edit();

            PREFERENCES_LOADED.set(editor, true);
            CommonPreference.THEME.setDefault(editor);

            UiPreference.AUTOMATIC_KEYBOARD.setDefault(editor);
            UiPreference.IGNORE_HISTORY_TIME.setDefault(editor);

            JourneyPreference.MAX_WALKING_DISTANCE.setDefault(editor);
            JourneyPreference.WALK_SPEED.setDefault(editor);
            JourneyPreference.SERVICE_TYPES.setDefault(editor);
            JourneyPreference.FARE_TYPES.setDefault(editor);

            editor.apply();
        }

        // Migrate the old data model into the new sqlite database.
        new DataResource().migrate(translinkApplication);
    }

}
