package com.lach.translink.network;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module(includes = NetworkInterceptorsModule.class)
public class NetworkModule {

    @Provides
    OkHttpClient providesOkHttpClient(List<Interceptor> interceptors) {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().addAll(interceptors); // Possible deubg specific interceptors.
        return client;
    }

}
