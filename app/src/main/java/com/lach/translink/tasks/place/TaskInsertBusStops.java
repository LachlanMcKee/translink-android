package com.lach.translink.tasks.place;

import android.content.Context;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Log;
import com.lach.translink.data.place.BusStop;
import com.lach.translink.data.place.BusStopDao;
import com.lach.translink.data.place.BusStopModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TaskInsertBusStops implements Task<Void> {
    private static final String TAG = TaskInsertBusStops.class.getSimpleName();

    // Common CSV known information
    private static final int CSV_COLUMN_COUNT = 16;
    private static final int CSV_ESTIMATED_ITEMS = 4000;

    // Known CSV indexes.
    private static final int COL_TRANSLINK_ID = 1;
    private static final int COL_DESCRIPTION = 2;
    private static final int COL_LATITUDE = 7;
    private static final int COL_LONGITUDE = 8;

    WeakReference<Context> contextRef;

    public TaskInsertBusStops(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    public AsyncResult<Void> execute(Object... params) {
        AsyncResult<Void> result;
        try {
            result = executeInternal();

        } catch (Exception ex) {
            Log.error(TAG, "An error occurred", ex);
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    public AsyncResult<Void> executeInternal() throws Exception {
        AsyncResult<Void> result = new AsyncResult<>(null);
        Context context = contextRef.get();
        if (context == null) {
            Log.warn(TAG, "Context not found.");
            return result;
        }

        Log.debug(TAG, "executeInternal");

        InputStreamReader is = new InputStreamReader(context.getAssets().open("bus_stops.csv"));
        contextRef.clear();

        BusStopDao dao = new BusStopDao();

        BufferedReader reader = new BufferedReader(is);
        reader.readLine();
        String line;
        List<BusStop> busStopList = new ArrayList<>(CSV_ESTIMATED_ITEMS);

        // This is an optimisation since we already know the number of columns.
        String[] values = new String[CSV_COLUMN_COUNT];

        while ((line = reader.readLine()) != null) {
            csvSplit(line, values);

            String translinkId = values[COL_TRANSLINK_ID];
            if (translinkId.isEmpty()) {
                continue;
            }

            BusStopModel model = dao.createModel();
            model.setTranslinkId(Long.parseLong(translinkId));
            model.setDescription(values[COL_DESCRIPTION]);
            model.setLatitude(Double.parseDouble(values[COL_LATITUDE]));
            model.setLongitude(Double.parseDouble(values[COL_LONGITUDE]));

            busStopList.add(model);
        }

        Log.debug(TAG, "Inserting " + busStopList.size() + " bus stops.");
        dao.insertRows(true, busStopList);
        Log.debug(TAG, "Finished inserting.");

        return result;
    }

    public static void csvSplit(String line, String[] values) {
        boolean withinQuotes = false;
        int start = 0, index = 0, length = line.length();

        for (int i = 0; i < length - 1; i++) {
            char currentChar = line.charAt(i);

            if (!withinQuotes && currentChar == ',') {
                values[index] = line.substring(start, i);
                index++;
                start = i + 1;

            } else if (currentChar == '"') {
                withinQuotes = !withinQuotes;
            }
        }
        values[index] = line.substring(start);
    }

}
