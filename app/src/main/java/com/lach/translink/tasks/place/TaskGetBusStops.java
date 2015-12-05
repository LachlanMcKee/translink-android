package com.lach.translink.tasks.place;

import com.google.android.gms.maps.model.LatLngBounds;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Log;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopDao;

import java.util.List;

import javax.inject.Inject;

public class TaskGetBusStops implements Task<List<BusStop>> {
    private static final String TAG = TaskGetBusStops.class.getSimpleName();
    private final BusStopDao busStopDao;

    @Inject
    public TaskGetBusStops(BusStopDao busStopDao) {
        this.busStopDao = busStopDao;
    }

    @Override
    public AsyncResult<List<BusStop>> execute(Object... params) {
        AsyncResult<List<BusStop>> result;
        try {
            result = executeInternal(params);

        } catch (Exception ex) {
            Log.error(TAG, "An error occurred", ex);
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    public AsyncResult<List<BusStop>> executeInternal(Object... params) throws Exception {
        Log.debug(TAG, "executeInternal");

        LatLngBounds latLngBounds = (LatLngBounds) params[0];

        List<BusStop> busStopList = (List<BusStop>) busStopDao.getBusStopsWithinRegion(latLngBounds);
        return new AsyncResult<>(busStopList);
    }

}
