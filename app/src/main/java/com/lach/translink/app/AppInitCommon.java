package com.lach.translink.app;

import android.app.Application;

import com.lach.translink.TranslinkApplication;
import com.lach.translink.util.DataResource;

public class AppInitCommon {

    /**
     * Should be called when the app is initially loaded.
     */
    public static void init(Application application) {
        // Migrate the old data model into the new sqlite database.
        new DataResource().migrate((TranslinkApplication) application);
    }

}
