package com.lach.translink.ui.presenter.resolve;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.map.MapBounds;
import com.lach.common.data.map.MapMarker;
import com.lach.common.data.map.MapPosition;
import com.lach.translink.data.place.bus.BusStop;
import com.lach.translink.data.place.bus.BusStopImpl;
import com.lach.translink.tasks.place.TaskGetBusStops;
import com.lach.translink.ui.impl.resolve.ResolveLocationEvents;
import com.lach.translink.ui.presenter.BasePresenterTest;
import com.lach.translink.ui.presenter.InMemoryViewState;
import com.lach.translink.ui.presenter.ViewState;
import com.lach.translink.ui.view.resolve.ResolveLocationMapView;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ResolveLocationMapPresenterTest extends BasePresenterTest<ResolveLocationMapPresenter, ResolveLocationMapView> {

    private static final MapPosition MAP_POSITION_1 = new MapPosition(-27.4679400, 153.0280900);
    private static final MapPosition MAP_POSITION_2 = new MapPosition(-27.4679401, 153.0280901);

    private static final BusStopMarker BUS_STOP_1;
    private static final BusStopMarker BUS_STOP_2;

    static {
        BUS_STOP_1 = new BusStopMarker();
        BUS_STOP_1.id = "1";
        BUS_STOP_1.busStop = new BusStopImpl(1);
        BUS_STOP_1.busStop.setLatitude(MAP_POSITION_1.getLatitude());
        BUS_STOP_1.busStop.setLongitude(MAP_POSITION_1.getLongitude());

        BUS_STOP_2 = new BusStopMarker();
        BUS_STOP_2.id = "2";
        BUS_STOP_2.busStop = new BusStopImpl(2);
        BUS_STOP_2.busStop.setLatitude(MAP_POSITION_2.getLatitude());
        BUS_STOP_2.busStop.setLongitude(MAP_POSITION_2.getLongitude());
    }

    private static final MapBounds MAP_BOUNDS_1 = new MapBounds(
            new MapPosition(-27.45, 153.02),
            new MapPosition(-27.46, 153.03)
    );

    TaskGetBusStops taskGetBusStops;

    @Test
    public void testMapClick() {
        presenter.onMapClick(MAP_POSITION_1);
        presenter.onMapClick(MAP_POSITION_2);

        MapClickVerify mapClickVerify = new MapClickVerify(view, 2);

        // We want the button to animate the first time.
        mapClickVerify.isContinueAnimated(0, true);
        mapClickVerify.isMarkerPositionEqualTo(0, MAP_POSITION_1.latitude, MAP_POSITION_1.longitude);

        // Ensure the second call does not animate, and we use the second map position.
        mapClickVerify.isContinueAnimated(1, false);
        mapClickVerify.isMarkerPositionEqualTo(1, MAP_POSITION_2.latitude, MAP_POSITION_2.longitude);
    }

    @Test
    public void testConfirmMapAddressMarker() {
        presenter.onMapClick(MAP_POSITION_1);

        setMockedEventBusAnswer(new EventBusAnswer() {
            @Override
            public void test(Object event) {
                assertEquals(event.getClass(), ResolveLocationEvents.MapAddressSelectedEvent.class);

                ResolveLocationEvents.MapAddressSelectedEvent castedEvent = (ResolveLocationEvents.MapAddressSelectedEvent) event;
                assertEquals(castedEvent.getPoint(), MAP_POSITION_1);
            }
        });

        presenter.onConfirmed();
    }

    @Test
    public void testInitialCameraChange() {
        cameraChanged(BUS_STOP_1);

        // Verify that the new bus stop marker is added to the view.
        ArgumentCaptor<MapPosition> mapPositionCaptor = ArgumentCaptor.forClass(MapPosition.class);
        ArgumentCaptor<Boolean> isSelectedCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(view, times(1)).addBusStopMarker(mapPositionCaptor.capture(), isSelectedCaptor.capture());

        assertEquals(mapPositionCaptor.getValue().latitude, BUS_STOP_1.getLatitude(), 0);
        assertEquals(mapPositionCaptor.getValue().longitude, BUS_STOP_1.getLongitude(), 0);
        assertFalse(isSelectedCaptor.getValue());
    }

    @Test
    public void testBusStopMarkerClicked() {
        cameraChanged(BUS_STOP_1);
        presenter.onMarkerClick(BUS_STOP_1.getId());

        verify(view, times(1)).bringMarkerToFront(any(MapMarker.class));
        verify(view, times(1)).moveCameraToBusStop(any(BusStop.class), anyBoolean(), any(ResolveLocationMapPresenterImpl.MoveCameraListener.class));
        verify(view, times(1)).showBusStopDetails(BUS_STOP_1.busStop, true);
    }

    @Test
    public void testBusStopMarkerConfirmed() {
        cameraChanged(BUS_STOP_1);
        presenter.onMarkerClick(BUS_STOP_1.getId());

        setMockedEventBusAnswer(new EventBusAnswer() {
            @Override
            public void test(Object event) {
                assertEquals(event.getClass(), ResolveLocationEvents.MapBusStopSelectedEvent.class);

                ResolveLocationEvents.MapBusStopSelectedEvent castedEvent = (ResolveLocationEvents.MapBusStopSelectedEvent) event;
                assertEquals(castedEvent.getBusStop(), BUS_STOP_1.busStop);
            }
        });

        presenter.onConfirmed();
    }

    @Test
    public void testAddressMarkerRecreated() {
        presenter.onMapClick(MAP_POSITION_1);

        // The life cycle should persist the selected marker, yet not animate the continue button on recreate.
        recreate();

        MapClickVerify mapClickVerify = new MapClickVerify(view, 2);
        mapClickVerify.isContinueAnimated(1, false);
        mapClickVerify.isMarkerPositionEqualTo(1, MAP_POSITION_1);
    }

    @Test
    public void testPersistedMapPosition() {
        assertEquals(presenter.getPersistedMapPosition(), null);

        // Map position should be created once a marker is clicked.
        cameraChanged(BUS_STOP_1);
        presenter.onMarkerClick(BUS_STOP_1.getId());

        MapPosition mapPosition = presenter.getPersistedMapPosition();
        assertEquals(mapPosition.latitude, BUS_STOP_1.getLatitude(), 0);
        assertEquals(mapPosition.longitude, BUS_STOP_1.getLongitude(), 0);

        // The map position should be removed during the destroy stage.
        ViewState viewState = new InMemoryViewState();
        presenter.saveState(viewState);
        presenter.onDestroy();

        assertEquals(presenter.getPersistedMapPosition(), null);

        // Map position should be retrieved from the save state.
        presenter.onCreate(view, viewState);
        mapPosition = presenter.getPersistedMapPosition();
        assertEquals(mapPosition.latitude, BUS_STOP_1.getLatitude(), 0);
        assertEquals(mapPosition.longitude, BUS_STOP_1.getLongitude(), 0);
    }

    @Override
    public ResolveLocationMapPresenter createPresenter() {
        taskGetBusStops = Mockito.mock(TaskGetBusStops.class);

        return new ResolveLocationMapPresenterImpl(new Provider<TaskGetBusStops>() {
            @Override
            public TaskGetBusStops get() {
                return taskGetBusStops;
            }
        });
    }

    @Override
    public Class<ResolveLocationMapView> getViewClass() {
        return ResolveLocationMapView.class;
    }

    @Override
    public void postSetup() {
        Mockito.when(view.addMarker(any(MapPosition.class))).thenAnswer(new Answer<MapMarker>() {
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

        Mockito.when(view.isMapReady()).thenReturn(true);
        Mockito.when(view.getMapZoomLevel()).thenReturn(15.0f);

        // Ensure the listener always fires when the method fires.
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ResolveLocationMapPresenterImpl.MoveCameraListener listener =
                        (ResolveLocationMapPresenterImpl.MoveCameraListener) invocation.getArguments()[2];

                listener.onFinish();
                return null;
            }
        }).when(view).moveCameraToBusStop(any(BusStop.class), anyBoolean(), any(ResolveLocationMapPresenterImpl.MoveCameraListener.class));
    }

    @Override
    public void postCreate() {
        // The map init will always be called after onCreate.
        presenter.onMapInit();
    }

    private void cameraChanged(final BusStopMarker... busStopMarkers) {
        // Avoid checking the map bounds and return a single bus stop result.
        Mockito.when(taskGetBusStops.execute(Mockito.anyVararg())).thenAnswer(new Answer<AsyncResult<List<BusStop>>>() {
            @Override
            public AsyncResult<List<BusStop>> answer(InvocationOnMock invocation) throws Throwable {
                List<BusStop> busStopList = new ArrayList<>(busStopMarkers.length);
                for (BusStopMarker stopMarker : busStopMarkers) {
                    busStopList.add(stopMarker.busStop);
                }
                return new AsyncResult<>(busStopList);
            }
        });

        Mockito.when(view.getMapBounds()).thenReturn(MAP_BOUNDS_1);

        // Adding the bus stop marker must be mocked, as the result is cached.
        Mockito.when(view.addBusStopMarker(any(MapPosition.class), anyBoolean())).thenAnswer(new Answer<MapMarker>() {
            @Override
            public MapMarker answer(InvocationOnMock invocation) throws Throwable {
                final MapPosition mapPosition = (MapPosition) invocation.getArguments()[0];
                String markerId = null;

                // Find the bus stop which has the matching lat/long
                for (BusStopMarker stopMarker : busStopMarkers) {
                    if (stopMarker.getLatitude() == mapPosition.getLatitude() && stopMarker.getLongitude() == mapPosition.getLongitude()) {
                        markerId = stopMarker.id;
                    }
                }
                final String finalMarkerId = markerId;

                return new MapMarker() {
                    @Override
                    public String getId() {
                        return finalMarkerId;
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
    }

    private static class BusStopMarker {
        String id;
        BusStop busStop;

        public String getId() {
            return id;
        }

        public double getLatitude() {
            return busStop.getLatitude();
        }

        public double getLongitude() {
            return busStop.getLongitude();
        }
    }

    private static class MapClickVerify {
        ArgumentCaptor<Boolean> animationCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<MapPosition> mapPositionCaptor = ArgumentCaptor.forClass(MapPosition.class);

        public MapClickVerify(ResolveLocationMapView view, int timesExpected) {
            verify(view, times(timesExpected)).showContinueButton(animationCaptor.capture());
            verify(view, times(timesExpected)).addMarker(mapPositionCaptor.capture());
        }

        public void isContinueAnimated(int index, boolean expectedValue) {
            assertEquals(animationCaptor.getAllValues().get(index), expectedValue);
        }

        public void isMarkerPositionEqualTo(int index, MapPosition mapPosition) {
            isMarkerPositionEqualTo(index, mapPosition.latitude, mapPosition.longitude);
        }

        public void isMarkerPositionEqualTo(int index, double latitude, double longitude) {
            MapPosition mapPosition = mapPositionCaptor.getAllValues().get(index);
            assertEquals(mapPosition.latitude, latitude, 0);
            assertEquals(mapPosition.longitude, longitude, 0);
        }
    }

}
