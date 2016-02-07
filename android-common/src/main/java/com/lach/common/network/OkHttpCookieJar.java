package com.lach.common.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class OkHttpCookieJar implements CookieJar {
    private final Map<String, List<Cookie>> cookieMap;

    public OkHttpCookieJar() {
        this.cookieMap = new HashMap<>();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();

        // Append an existing list if it exists.
        List<Cookie> storedCookies = cookieMap.get(host);
        if (storedCookies == null) {
            storedCookies = new ArrayList<>();
        }
        storedCookies.addAll(cookies);

        cookieMap.put(host, storedCookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieMap.get(url.host());

        if (cookies == null) {
            return Collections.emptyList();
        }

        return cookies;
    }

}