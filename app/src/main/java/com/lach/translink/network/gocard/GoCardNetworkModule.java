package com.lach.translink.network.gocard;

import com.lach.common.data.CoreModule;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.network.OkHttpCookieJar;
import com.lach.translink.network.NetworkModule;
import com.lach.translink.network.UserAgentInterceptor;
import com.lach.translink.tasks.gocard.TaskGoCardDetails;
import com.lach.translink.tasks.gocard.TaskGoCardHistory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module(includes = {NetworkModule.class, CoreModule.class})
public class GoCardNetworkModule {

    @Provides
    public GoCardCredentials providesGoCardCredentials(PreferencesProvider preferencesProvider) {
        return new GoCardCredentialsImpl(preferencesProvider);
    }

    @Provides
    @Singleton
    public GoCardHttpClient providesGoCardHttpClient(OkHttpClient.Builder httpClientBuilder, GoCardCredentials goCardCredentials) {
        httpClientBuilder.cookieJar(new OkHttpCookieJar());
        httpClientBuilder.addInterceptor(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36"));

        return new GoCardHttpClientImpl(httpClientBuilder.build(), goCardCredentials);
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
