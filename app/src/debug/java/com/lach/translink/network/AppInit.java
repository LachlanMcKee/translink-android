package com.lach.translink.network;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class AppInit {

    /**
     * Should be called when the app is initially loaded.
     */
    public static void init(Application application) {
        // Init the Stetho testing library for debug builds.
        Stetho.initialize(
                Stetho.newInitializerBuilder(application)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(application))
                        .build());
    }

}
