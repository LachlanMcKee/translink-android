package com.lach.translink.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class NetworkInterceptorsModule {

    @Provides
    public List<Interceptor> providesNetworkInterceptors() {
        List<Interceptor> networkInterceptors = new ArrayList<>();
        networkInterceptors.add(new StethoInterceptor());

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        networkInterceptors.add(loggingInterceptor);

        return networkInterceptors;
    }
}
