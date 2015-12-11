package com.lach.translink.network;

import android.support.annotation.WorkerThread;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

public class GoCardHttpClient extends OkHttpClient {
    private final GoCardCredentials goCardCredentials;

    private boolean hasInitialised = false;

    @Inject
    public GoCardHttpClient(GoCardCredentials goCardCredentials) {
        this.goCardCredentials = goCardCredentials;
    }

    @SuppressWarnings("UnusedReturnValue")
    @WorkerThread
    public synchronized boolean init() {
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

        Response response;
        String responseBody;
        try {
            response = newCall(request).execute();
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

}
