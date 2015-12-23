package com.lach.translink.network;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

public class GoCardHttpClientImpl implements GoCardHttpClient {
    private final OkHttpClient okHttpClient;
    private final GoCardCredentials goCardCredentials;

    private boolean hasInitialised = false;

    @Inject
    public GoCardHttpClientImpl(OkHttpClient okHttpClient, GoCardCredentials goCardCredentials) {
        this.okHttpClient = okHttpClient;
        this.goCardCredentials = goCardCredentials;
    }

    private boolean init() {
        if (hasInitialised) {
            return true;
        }

        String url;
        try {
            url = String.format("https://gocard.translink.com.au/webtix/welcome/welcome.do?cardOps=Display&cardNum=%s&pass=%s",
                    goCardCredentials.getCardNumber(), URLEncoder.encode(goCardCredentials.getPassword(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        com.squareup.okhttp.Response response;
        String responseBody;
        try {
            response = okHttpClient.newCall(request).execute();
            responseBody = response.body().string();
        } catch (Exception ignored) {
            return false;
        }

        if (!response.isSuccessful() || responseBody.contains("There was a problem")) {
            return false;
        }

        hasInitialised = true;
        return true;
    }

    public synchronized Response getResponseForUrl(String url) throws IOException {
        if (!init()) {
            return new Response(false, null);
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        com.squareup.okhttp.Response response = okHttpClient.newCall(request).execute();

        String content = null;
        boolean isSuccessful = response.isSuccessful();
        if (isSuccessful) {
            content = response.body().string();
        }

        return new Response(isSuccessful, content);
    }

}
