package com.lach.translink.tasks.place;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.map.MapBounds;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopDao;

import java.util.List;

import javax.inject.Inject;

public class TaskGetBusStops implements Task<List<BusStop>> {
    private final BusStopDao busStopDao;

    @Inject
    public TaskGetBusStops(BusStopDao busStopDao) {
        this.busStopDao = busStopDao;
    }

    @Override
    public AsyncResult<List<BusStop>> execute(Object... params) {
        MapBounds bounds = (MapBounds) params[0];

        //noinspection unchecked
        List<BusStop> busStopList = (List<BusStop>) busStopDao.getBusStopsWithinRegion(bounds);
        return new AsyncResult<>(busStopList);
    }

}
