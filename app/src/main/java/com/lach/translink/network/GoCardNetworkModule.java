package com.lach.translink.network;

import com.lach.common.data.CoreModule;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.translink.data.gocard.GoCardPreference;
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
    @GoCardNumber
    public String providesGoCardNumber(PreferencesProvider preferencesProvider) {
        return GoCardPreference.CARD_NUMBER.get(preferencesProvider.getPreferences());
    }

    @Provides
    @GoCardPassword
    public String providesGoCardPassword(PreferencesProvider preferencesProvider) {
        return GoCardPreference.PASSWORD.get(preferencesProvider.getPreferences());
    }

    @Provides
    @GoCardNumberValid
    public boolean providesGoCardNumberValid(PreferencesProvider preferencesProvider) {
        return GoCardPreference.CARD_NUMBER.exists(preferencesProvider.getPreferences());
    }

    @Provides
    @GoCardPasswordValid
    public boolean providesGoCardPasswordValid(PreferencesProvider preferencesProvider) {
        return GoCardPreference.PASSWORD.exists(preferencesProvider.getPreferences());
    }

    @Provides
    @Singleton
    public GoCardHttpClient providesGoCardOkHttpClient(List<Interceptor> networkInterceptors, @GoCardNumber String cardNumber, @GoCardPassword String password) {
        GoCardHttpClient client = new GoCardHttpClient(cardNumber, password);

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
