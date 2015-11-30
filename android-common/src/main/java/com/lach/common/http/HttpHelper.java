package com.lach.common.http;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public class HttpHelper {

    public static String post(String urlString, Map<String, String> params) throws IOException, UnexpectedResponseException {
        // Construct data
        String data = "";

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
        }

        // Send data
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), data);
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response != null && response.code() == 200) {
            return response.body().string();
        }

        String message;
        if (response == null) {
            message = "response is null";
        } else {
            message = response.toString();
        }

        // The server returned an invalid response.
        throw new UnexpectedResponseException(message);
    }

}
