package com.lach.translink.network;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.okhttp.Interceptor;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = NetworkModule.class)
public class GoCardNetworkModule {
    private final Application app;

    public GoCardNetworkModule(Application app) {
        this.app = app;
    }

    @Provides
    public Application providesApplication() {
        return app;
    }

    @Provides
    public SharedPreferences providesSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @GoCardNumber
    public String providesGoCardNumber(SharedPreferences preferences) {
        return preferences.getString("cardNum", "");
    }

    @Provides
    @GoCardPassword
    public String providesGoCardPassword(SharedPreferences preferences) {
        return preferences.getString("cardPass", "");
    }

    @Provides
    @GoCardNumberValid
    public boolean providesGoCardNumberValid(SharedPreferences preferences) {
        return preferences.contains("cardNum");
    }

    @Provides
    @GoCardPasswordValid
    public boolean providesGoCardPasswordValid(SharedPreferences preferences) {
        return preferences.contains("cardPass");
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
