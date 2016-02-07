package com.lach.translink.network;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module(includes = NetworkInterceptorsModule.class)
public class NetworkModule {

    @Provides
    OkHttpClient.Builder providesHttpClientBuilder(List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.networkInterceptors().addAll(interceptors); // Possible deubg specific interceptors.
        return builder;
    }

    @Provides
    OkHttpClient providesHttpClient(OkHttpClient.Builder builder) {
        return builder.build();
    }

}
