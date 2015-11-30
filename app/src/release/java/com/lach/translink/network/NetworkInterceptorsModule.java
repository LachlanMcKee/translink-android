package com.lach.translink.network;

import com.squareup.okhttp.Interceptor;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkInterceptorsModule {

    @Provides
    public List<Interceptor> providesNetworkInterceptors() {
        return new ArrayList<>();
    }
}
