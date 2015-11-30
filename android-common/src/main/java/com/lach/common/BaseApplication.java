package com.lach.common;

import android.app.Activity;
import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public abstract class BaseApplication extends Application {

    private static Bus mEventBus;

    public static Bus getEventBus() {
        return mEventBus;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mEventBus = new Bus(ThreadEnforcer.MAIN);
    }

    public abstract void applyTheme(Activity activity, boolean useActionBar);

    public abstract boolean isLightTheme(Activity activity);

}
