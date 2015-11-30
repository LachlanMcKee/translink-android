package com.lach.translink.webview;

import com.lachlanm.xwalkfallback.CookieManagerFacade;
import com.lachlanm.xwalkfallback.WebViewFacade;
import com.lachlanm.xwalkfallback.WebViewFacadeFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class WebViewModule {

    @Provides
    public WebViewFacade providesWebViewFacade() {
        return new WebViewFacadeFactory().makeWebViewFacade();
    }

    @Provides
    public CookieManagerFacade providesCookieManagerFacade() {
        return new WebViewFacadeFactory().makeCookieManagerFacade();
    }

}
