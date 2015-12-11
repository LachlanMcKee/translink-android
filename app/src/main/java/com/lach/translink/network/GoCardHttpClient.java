package com.lach.translink.network;

import java.io.IOException;

public interface GoCardHttpClient {
    Response getResponseForUrl(String url) throws IOException;

    class Response {
        public final boolean isSuccess;
        public final String content;

        public Response(boolean isSuccess, String content) {
            this.isSuccess = isSuccess;
            this.content = content;
        }
    }
}
