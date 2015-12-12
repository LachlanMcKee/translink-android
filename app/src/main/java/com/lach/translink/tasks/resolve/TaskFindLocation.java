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
import com.lach.common.http.HttpHelper;
import com.lach.common.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskFindLocation implements Task<ArrayList<String>> {
    private static final String TAG = "TaskFindLocation";

    private static final String FIND_LOCATION_URL = "http://mobile.jp.translink.com.au/travel-information/journey-planner/find-location";

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

        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("location", location);

        Log.debug(TAG, "Starting download");
        String responseBody = HttpHelper.post(FIND_LOCATION_URL, httpParams);

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
}
