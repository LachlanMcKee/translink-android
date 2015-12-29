package com.lach.translink.ui.presenter.resolve;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.UnitTestingTaskBuilder;
import com.lach.common.async.Task;
import com.lach.common.async.TaskBuilder;
import com.lach.common.data.map.MapBounds;
import com.lach.common.data.map.MapMarker;
import com.lach.common.data.map.MapPosition;
import com.lach.translink.BaseTest;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopImpl;
import com.lach.translink.tasks.place.TaskGetBusStops;
import com.lach.translink.ui.view.resolve.ResolveLocationMapView;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import static org.mockito.Mockito.times;

public class ResolveLocationMapPresenterTest extends BaseTest {

    private static final MapPosition MAP_POSITION_1 = new MapPosition(-27.4679400, 153.0280900);
    private static final MapPosition MAP_POSITION_2 = new MapPosition(-27.4679401, 153.0280901);

    private static final MapBounds MAP_BOUNDS_1 = new MapBounds(
            new MapPosition(-27.45, 153.02),
            new MapPosition(-27.46, 153.03)
    );

    @Mock
    TaskGetBusStops taskGetBusStops;

    @Mock
    ResolveLocationMapView view;

    ResolveLocationMapPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        presenter = new ResolveLocationMapPresenterImpl(new Provider<TaskGetBusStops>() {
            @Override
            public TaskGetBusStops get() {
                return taskGetBusStops;
            }
        });

        // Add task handling to prevent need for Android fragments.
        Mockito.when(view.createTask(Mockito.anyInt(), Mockito.any(Task.class))).thenAnswer(new Answer<TaskBuilder>() {
            @Override
            public TaskBuilder answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                return new UnitTestingTaskBuilder((int) arguments[0], (Task) arguments[1], new UnitTestingTaskBuilder.PostExecuteListener() {
                    @Override
                    public void onTaskExecuted(int taskId, AsyncResult result) {
                        int errorId = result.getErrorId();

                        if (!result.hasError()) {
                            presenter.onTaskFinished(taskId, result);
                        } else {
                            presenter.onTaskError(taskId, errorId);
                        }
                    }
                });
            }
        });

        Mockito.when(view.addMarker(Mockito.any(MapPosition.class))).thenAnswer(new Answer<MapMarker>() {
            @Override
            public MapMarker answer(InvocationOnMock invocation) throws Throwable {
                final MapPosition mapPosition = (MapPosition) invocation.getArguments()[0];

                return new MapMarker() {
                    @Override
                    public String getId() {
                        return "1";
                    }

                    @Override
                    public MapPosition getMapPosition() {
                        return mapPosition;
                    }

                    @Override
                    public Object getMarker() {
                        return null;
                    }
                };
            }
        });
    }

    private void mapInit() {
        presenter.onCreate(view, null);
        presenter.onMapInit();

        Mockito.when(view.isMapReady()).thenReturn(true);
        Mockito.when(view.getMapZoomLevel()).thenReturn(15.0f);
    }

    @Test
    public void testInitialMapClick() {
        mapInit();
        presenter.onMapClick(MAP_POSITION_1);

        // We want the button to animate the first time.
        Mockito.verify(view, times(1)).showContinueButton(true);
        Mockito.verify(view, times(1)).addMarker(MAP_POSITION_1);
    }

    @Test
    public void testSecondaryMapClick() {
        mapInit();
        presenter.onMapClick(MAP_POSITION_1);
        presenter.onMapClick(MAP_POSITION_2);

        // We don't want the button to animate the second time.
        ArgumentCaptor<Boolean> animationCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<MapPosition> mapPositionCaptor = ArgumentCaptor.forClass(MapPosition.class);

        Mockito.verify(view, times(2)).showContinueButton(animationCaptor.capture());
        Mockito.verify(view, times(2)).addMarker(mapPositionCaptor.capture());

        // Ensure the second call does not animate, and we use the second map position.
        Assert.assertEquals(animationCaptor.getAllValues().get(1), false);
        Assert.assertEquals(mapPositionCaptor.getAllValues().get(1), MAP_POSITION_2);
    }

    @Test
    public void testInitialCameraChange() {
        mapInit();

        // Avoid checking the map bounds and return a single bus stop result.
        Mockito.when(taskGetBusStops.execute(Mockito.anyVararg())).thenAnswer(new Answer<AsyncResult<List<BusStop>>>() {
            @Override
            public AsyncResult<List<BusStop>> answer(InvocationOnMock invocation) throws Throwable {
                List<BusStop> results = new ArrayList<>();

                BusStopImpl busStop = new BusStopImpl(1);
                busStop.setLatitude(MAP_POSITION_1.getLatitude());
                busStop.setLongitude(MAP_POSITION_1.getLongitude());
                results.add(busStop);

                return new AsyncResult<>(results);
            }
        });

        Mockito.when(view.getMapBounds()).thenReturn(MAP_BOUNDS_1);

        // Adding the bus stop marker must be mocked, as the result is cached.
        Mockito.when(view.addBusStopMarker(Mockito.any(MapPosition.class), Mockito.anyBoolean())).thenAnswer(new Answer<MapMarker>() {
            @Override
            public MapMarker answer(InvocationOnMock invocation) throws Throwable {
                final MapPosition mapPosition = (MapPosition) invocation.getArguments()[0];

                return new MapMarker() {
                    @Override
                    public String getId() {
                        return "1";
                    }

                    @Override
                    public MapPosition getMapPosition() {
                        return mapPosition;
                    }

                    @Override
                    public Object getMarker() {
                        return null;
                    }
                };
            }
        });

        // Prompt a change in map camera which will result in the bus stop task being executed.
        presenter.onCameraChange();

        // Verify that the new bus stop marker is added to the view.
        ArgumentCaptor<MapPosition> mapPositionCaptor = ArgumentCaptor.forClass(MapPosition.class);
        ArgumentCaptor<Boolean> isSelectedCaptor = ArgumentCaptor.forClass(Boolean.class);

        Mockito.verify(view, times(1)).addBusStopMarker(mapPositionCaptor.capture(), isSelectedCaptor.capture());

        Assert.assertEquals(mapPositionCaptor.getValue().latitude, MAP_POSITION_1.getLatitude(), 0);
        Assert.assertEquals(mapPositionCaptor.getValue().longitude, MAP_POSITION_1.getLongitude(), 0);
        Assert.assertFalse(isSelectedCaptor.getValue());
    }

}
