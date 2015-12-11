package com.lach.translink.network;

import com.lach.common.data.CoreModule;
import com.lach.common.data.preference.PreferencesProvider;
import com.squareup.okhttp.Interceptor;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {NetworkModule.class, CoreModule.class})
public class GoCardNetworkModule {

    @Provides
    public GoCardCredentials providesGoCardCredentials(PreferencesProvider preferencesProvider) {
        return new GoCardCredentialsImpl(preferencesProvider);
    }

    @Provides
    @Singleton
    public GoCardHttpClient providesGoCardOkHttpClient(List<Interceptor> networkInterceptors, GoCardCredentials goCardCredentials) {
        GoCardHttpClient client = new GoCardHttpClient(goCardCredentials);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        client.setCookieHandler(cookieManager);

        client.setConnectTimeout(5, TimeUnit.SECONDS);
        client.setReadTimeout(5, TimeUnit.SECONDS);

        client.networkInterceptors().add(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36"));
        client.networkInterceptors().addAll(networkInterceptors);

        return client;
    }

}
