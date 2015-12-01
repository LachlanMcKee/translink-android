package com.lach.translink.app;

import android.app.Application;

public class AppInit {

    /**
     * Should be called when the app is initially loaded.
     */
    public static void init(Application application) {
        AppInitCommon.init(application);
    }

}
