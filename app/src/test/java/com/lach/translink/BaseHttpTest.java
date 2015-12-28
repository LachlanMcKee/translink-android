package com.lach.translink;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;

@PrepareForTest(value = {Response.class, ResponseBody.class})
public abstract class BaseHttpTest extends BaseTest {

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
