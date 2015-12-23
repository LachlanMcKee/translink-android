package com.lach.translink.network;

import com.lach.common.data.CoreModule;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.translink.tasks.gocard.TaskGoCardDetails;
import com.lach.translink.tasks.gocard.TaskGoCardHistory;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

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
    public GoCardHttpClient providesGoCardOkHttpClient(OkHttpClient okHttpClient, GoCardCredentials goCardCredentials) {
        GoCardHttpClientImpl client = new GoCardHttpClientImpl(okHttpClient, goCardCredentials);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        okHttpClient.setCookieHandler(cookieManager);

        okHttpClient.networkInterceptors().add(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36"));
        return client;
    }

    @Provides
    public TaskGoCardDetails providesTaskGoCardDetails(GoCardHttpClient goCardHttpClient) {
        return new TaskGoCardDetails(goCardHttpClient);
    }

    @Provides
    public TaskGoCardHistory providesTaskGoCardHistory(GoCardHttpClient goCardHttpClient) {
        return new TaskGoCardHistory(goCardHttpClient);
    }

}
