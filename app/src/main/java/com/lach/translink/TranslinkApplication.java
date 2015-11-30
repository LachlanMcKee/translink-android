package com.lach.translink;

import android.app.Activity;

import com.lach.common.BaseApplication;
import com.lach.common.data.CoreModule;
import com.lach.common.log.Instrumentation;
import com.lach.translink.data.DaggerDataComponentImpl;
import com.lach.translink.data.DataComponent;
import com.lach.translink.network.AppInit;
import com.lach.translink.network.DaggerGoCardNetworkComponentImpl;
import com.lach.translink.network.GoCardNetworkComponent;
import com.lach.translink.network.GoCardNetworkModule;
import com.lach.translink.webview.DaggerWebViewComponent;
import com.lach.translink.webview.WebViewComponent;
import com.lachlanm.xwalkfallback.WebViewFacade;
import com.raizlabs.android.dbflow.config.FlowManager;

import javax.inject.Inject;
import javax.inject.Provider;

public class TranslinkApplication extends BaseApplication {

    private CoreModule coreModule;
    private DataComponent dataComponent;
    private Provider<GoCardNetworkComponent> goCardNetworkComponentProvider;

    private boolean webViewBeenInitialised;

    @Inject
    WebViewFacade webViewFacade;

    @Override
    public void onCreate() {
        super.onCreate();

        // Load any application specific logic. This can be overridden within a build type.
        AppInit.init(this);

        // Loads the instrumentation which sends bug reports and crashes.
        Instrumentation.init(this, "364904c2");

        // Loads the DbFlow manager used for database handling.
        FlowManager.init(this);

        goCardNetworkComponentProvider = new Provider<GoCardNetworkComponent>() {
            @Override
            public GoCardNetworkComponent get() {
                return DaggerGoCardNetworkComponentImpl.builder()
                        .goCardNetworkModule(new GoCardNetworkModule(TranslinkApplication.this))
                        .build();
            }
        };
    }

    public synchronized CoreModule getCoreModule() {
        if (coreModule == null) {
            coreModule = new CoreModule(this);
        }
        return coreModule;
    }

    public synchronized DataComponent getDataComponent() {
        if (dataComponent == null) {
            dataComponent = DaggerDataComponentImpl.builder()
                    .coreModule(getCoreModule())
                    .build();
        }
        return dataComponent;
    }

    public void setDataComponent(DataComponent locationDataComponent) {
        this.dataComponent = locationDataComponent;
    }

    public synchronized void initWebViewIfRequired(Activity activity) {
        if (webViewBeenInitialised) {
            return;
        }
        webViewBeenInitialised = true;

        WebViewComponent webViewComponent = DaggerWebViewComponent.builder()
                .coreModule(getCoreModule())
                .build();

        webViewComponent.inject(this);
        webViewFacade.getView(activity);
        webViewFacade.destroy();
        webViewFacade = null;
    }

    public void setGoCardNetworkComponentProvider(Provider<GoCardNetworkComponent> goCardNetworkComponentProvider) {
        this.goCardNetworkComponentProvider = goCardNetworkComponentProvider;
    }

    public GoCardNetworkComponent createGoCardNetworkComponent() {
        return goCardNetworkComponentProvider.get();
    }
}
