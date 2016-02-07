package com.lach.translink.network.gocard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class GoCardHttpClientImpl implements GoCardHttpClient {
    private final OkHttpClient httpClient;
    private final GoCardCredentials goCardCredentials;

    private boolean hasInitialised = false;

    @Inject
    public GoCardHttpClientImpl(OkHttpClient httpClient, GoCardCredentials goCardCredentials) {
        this.httpClient = httpClient;
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

        okhttp3.Response response;
        String responseBody;
        try {
            response = httpClient.newCall(request).execute();
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

        okhttp3.Response response = httpClient.newCall(request).execute();

        String content = null;
        boolean isSuccessful = response.isSuccessful();
        if (isSuccessful) {
            content = response.body().string();
        }

        return new Response(isSuccessful, content);
    }

}
