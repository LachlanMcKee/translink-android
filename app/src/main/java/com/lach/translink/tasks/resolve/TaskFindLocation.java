package com.lach.translink.tasks.resolve;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Log;
import com.lach.common.network.UnexpectedResponseException;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TaskFindLocation implements Task<ArrayList<String>> {
    private static final String TAG = "TaskFindLocation";
    private static final String FIND_LOCATION_URL = "http://mobile.jp.translink.com.au/travel-information/journey-planner/find-location";

    public static Object[] createParams(String location) {
        Object[] params = new Object[1];
        params[0] = location;
        return params;
    }

    private OkHttpClient httpClient;

    @Inject
    public TaskFindLocation(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AsyncResult<ArrayList<String>> execute(Object... params) {
        AsyncResult<ArrayList<String>> result;
        try {
            result = executeInternal(params);
        } catch (Exception ex) {
            Log.warn(TAG, "An error occurred", ex);
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    private AsyncResult<ArrayList<String>> executeInternal(Object... params) throws Exception {
        String location = (String) params[0];

        Log.debug(TAG, "Starting download");
        String responseBody = getLocationResponse(location);

        Log.debug(TAG, "Finished downloading content");

        JsonParser jp = new JsonParser();
        JsonElement parsedResult;
        try {
            parsedResult = jp.parse(responseBody);
        } catch (JsonSyntaxException ex) {
            // Add some custom logging for any poorly formed json.
            Throwable cause = ex.getCause();

            if (cause != null && cause instanceof MalformedJsonException) {
                Log.error(TAG, "Dodgy Json: " + responseBody + ", location: " + location, ex);
                return null;
            }

            throw ex;
        }
        JsonArray jArray = parsedResult.getAsJsonArray();

        ArrayList<String> addressList = new ArrayList<>();
        for (int i = 0; i < jArray.size(); i++) {
            JsonObject jsonObject = jArray.get(i).getAsJsonObject();
            addressList.add(jsonObject.get("Description").getAsString());
        }
        Log.debug(TAG, "Finished parsing locations");

        return new AsyncResult<>(addressList);
    }

    private String getLocationResponse(String location) throws IOException, UnexpectedResponseException {
        FormBody.Builder formData = new FormBody.Builder();
        formData.add("location", location);

        // Send data
        Request request = new Request.Builder()
                .url(FIND_LOCATION_URL)
                .post(formData.build())
                .build();

        Response response = httpClient.newCall(request).execute();
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
