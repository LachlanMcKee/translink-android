package com.lach.translink.tasks.place;

import android.content.Context;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Log;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopDao;
import com.lach.translink.tasks.gocard.BaseTaskGoCardTest;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TaskInsertBusStopsTest extends BaseTaskGoCardTest {
    private static final String TAG = TaskInsertBusStopsTest.class.getSimpleName();

    @Test
    public void testInsertSuccess() {
        Context mockContext = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        BusStopDao mockedBusStopDao = Mockito.spy(new BusStopDao());

        try {
            Mockito.when(mockContext.getAssets().open(Mockito.anyString())).thenAnswer(new Answer<InputStream>() {
                @Override
                public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return ClassLoader.getSystemResourceAsStream("place/bus_stops_sample.csv");
                }
            });
        } catch (IOException ex) {
            Log.error(TAG, "Error during mocking", ex);
        }

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                List<BusStop> busStopList = (List<BusStop>) invocation.getArguments()[1];
                Assert.assertTrue(busStopList.size() == 2);

                // Ensure that the values were extracted correctly.
                BusStop busStop1 = busStopList.get(0);
                BusStop busStop2 = busStopList.get(1);

                Assert.assertEquals(busStop1.getDescription(), "Turbot St F/S Wharf St (Stop 4)");
                Assert.assertEquals(busStop2.getDescription(), "Ann Street F/S Orient Hotel (Stop 5)");

                Assert.assertEquals(busStop1.getStationId(), "000004");
                Assert.assertEquals(busStop2.getStationId(), "000005");

                Assert.assertEquals(busStop1.getLatitude(), -27.463306);
                Assert.assertEquals(busStop2.getLatitude(), -27.462917);

                Assert.assertEquals(busStop1.getLongitude(), 153.028396);
                Assert.assertEquals(busStop2.getLongitude(), 153.030586);

                return null;
            }
        }).when(mockedBusStopDao).insertRows(Mockito.anyBoolean(), Mockito.anyListOf(BusStop.class));

        TaskInsertBusStops taskInsertBusStops = new TaskInsertBusStops(mockContext, mockedBusStopDao);
        taskInsertBusStops.execute();
    }

    @Test
    public void testInsertFailure() {
        Context mockContext = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        BusStopDao mockedBusStopDao = Mockito.spy(new BusStopDao());

        try {
            Mockito.when(mockContext.getAssets().open(Mockito.anyString())).thenThrow(new IOException());
        } catch (IOException ex) {
            Log.error(TAG, "Error during mocking", ex);
        }

        TaskInsertBusStops taskInsertBusStops = new TaskInsertBusStops(mockContext, mockedBusStopDao);
        AsyncResult<Void> execute = taskInsertBusStops.execute();

        // We expect an io failure error message to be returned.
        Assert.assertEquals(execute.getErrorId(), TaskGenericErrorType.IO_FAILURE);
    }

}
