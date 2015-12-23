package com.lach.translink.network;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

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
