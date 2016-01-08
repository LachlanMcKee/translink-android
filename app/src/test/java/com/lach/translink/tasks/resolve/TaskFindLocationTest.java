package com.lach.translink.tasks.resolve;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.translink.BaseHttpTest;
import com.squareup.okhttp.OkHttpClient;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class TaskFindLocationTest extends BaseHttpTest {

    @Test
    public void testFindSuccess() throws IOException {
        AsyncResult<ArrayList<String>> result = mockLocationResponse(200, readFileFromResources("resolve/find_location_sample.json"));

        Assert.assertFalse(result.hasError());
        Assert.assertEquals(result.getItem().size(), 5);
    }

    @Test
    public void testFindError() throws IOException {
        testErrorResponse(201);
        testErrorResponse(203);
        testErrorResponse(400);
        testErrorResponse(403);
        testErrorResponse(500);
    }

    private void testErrorResponse(int responseCode) throws IOException {
        AsyncResult<ArrayList<String>> result = mockLocationResponse(responseCode, null);

        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getErrorId(), TaskGenericErrorType.INVALID_NETWORK_RESPONSE);
    }

    private AsyncResult<ArrayList<String>> mockLocationResponse(int responseCode, String jsonContent) throws IOException {
        OkHttpClient client = mockHttpResponse(jsonContent, responseCode);

        TaskFindLocation taskFindLocation = new TaskFindLocation(client);
        return taskFindLocation.execute(TaskFindLocation.createParams("Wyn"));
    }

}
