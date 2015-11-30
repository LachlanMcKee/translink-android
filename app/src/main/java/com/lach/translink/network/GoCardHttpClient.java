package com.lach.translink.network;

import android.support.annotation.WorkerThread;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoCardHttpClient extends OkHttpClient {
    private final String cardNumber;
    private final String password;

    private boolean hasInitialised = false;

    public GoCardHttpClient(String cardNumber, String password) {
        this.cardNumber = cardNumber;
        this.password = password;
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
                    cardNumber, URLEncoder.encode(password, "utf-8"));
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
