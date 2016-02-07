package com.lach.translink;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@PrepareForTest(value = {Response.class, ResponseBody.class, OkHttpClient.Builder.class})
public abstract class BaseHttpTest extends BaseTest {

    public OkHttpClient.Builder mockHttpClientBuilder(final HttpResponseHandler responseHandler) throws IOException {
        OkHttpClient.Builder builder = PowerMockito.mock(OkHttpClient.Builder.class);
        PowerMockito.when(builder.build()).thenAnswer(new Answer<Call.Factory>() {
            @Override
            public OkHttpClient answer(InvocationOnMock invocation) throws Throwable {
                return mockHttpClient(responseHandler);
            }
        });
        return builder;
    }

    public OkHttpClient mockHttpClient(final HttpResponseHandler responseHandler) throws IOException {
        OkHttpClient client = Mockito.mock(OkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(client.newCall(Mockito.any(Request.class)).execute()).thenAnswer(new Answer<Response>() {
            int responseCount = 0;

            @Override
            public Response answer(InvocationOnMock invocation) throws Throwable {
                return responseHandler.getResponse(responseCount++);
            }
        });
        return client;
    }

    public Response createMockedResponse(String content) throws IOException {
        return createMockedResponse(content, 200);
    }

    public Response createMockedResponse(String content, int responseCode) throws IOException {
        Response response = PowerMockito.mock(Response.class, Mockito.RETURNS_MOCKS);
        ResponseBody responseBody = PowerMockito.mock(ResponseBody.class, Mockito.RETURNS_MOCKS);

        PowerMockito.when(response.code()).thenReturn(responseCode);
        PowerMockito.when(response.body()).thenReturn(responseBody);
        PowerMockito.when(responseBody.string()).thenReturn(content);

        return response;
    }

    public OkHttpClient mockHttpResponse(final String content, final int responseCode) throws IOException {
        return mockHttpClient(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                return createMockedResponse(content, responseCode);
            }
        });
    }

    public interface HttpResponseHandler {
        Response getResponse(int responseIndex) throws IOException;
    }

}
