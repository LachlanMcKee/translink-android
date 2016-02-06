package com.lach.translink.tasks.result;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.preference.InMemoryPreferences;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.translink.BaseHttpTest;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.data.place.bus.BusStopDao;
import com.lachlanm.xwalkfallback.CookieManagerFacade;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.util.Date;

public class TaskJourneySearchTest extends BaseHttpTest {

    @Test
    public void testSearchSuccess() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                Response response = createMockedResponse("");
                Response networkResponse = createNetworkResponse("your-travel-options");
                PowerMockito.when(response.networkResponse()).thenReturn(networkResponse);
                return response;
            }
        });

        Assert.assertFalse(result.hasError());
    }

    @Test
    public void testSearchSuccessWithRedirects() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                Response response = createMockedResponse("");
                Response networkResponse = createNetworkResponse("your-travel-options");

                if (responseIndex == 0) {
                    PowerMockito.when(response.isRedirect()).thenReturn(true);
                }

                PowerMockito.when(response.networkResponse()).thenReturn(networkResponse);
                return response;
            }
        });

        Assert.assertFalse(result.hasError());
    }

    @Test
    public void testSearchRecoverableError() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                if (responseIndex == 0) {
                    return createMockedResponse(readFileFromResources("result/search_bad_start_location.html"));
                }

                Response response = createMockedResponse("");
                Response networkResponse = createNetworkResponse("your-travel-options");
                PowerMockito.when(response.networkResponse()).thenReturn(networkResponse);
                return response;
            }
        });

        Assert.assertFalse(result.hasError());
    }

    @Test
    public void testSearchErrorConfirmLocation() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                Response response = createMockedResponse("");
                Response networkResponse = createNetworkResponse("confirm-location");
                PowerMockito.when(response.networkResponse()).thenReturn(networkResponse);
                return response;
            }
        });

        checkForErrorId(result, TaskJourneySearch.ERROR_INVALID_LOCATION);
    }

    @Test
    public void testSearchErrorInvalidDate() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                return createMockedResponse(readFileFromResources("result/search_invalid_date.html"));
            }
        });

        checkForErrorId(result, TaskJourneySearch.ERROR_INVALID_DATE);
    }

    @Test
    public void testSearchErrorInvalidContent() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                // Test when a null body is passed.
                return createMockedResponse(null);
            }
        });

        checkForErrorId(result, TaskJourneySearch.ERROR_INVALID_CONTENT);

        result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                // Test when any text is returned in the body.
                return createMockedResponse("Dummy content");
            }
        });

        checkForErrorId(result, TaskJourneySearch.ERROR_INVALID_CONTENT);
    }

    @Test
    public void testSearchErrorInvalidResults() throws IOException {
        AsyncResult<TaskJourneySearch.JourneyResponse> result = executeTask(new HttpResponseHandler() {
            @Override
            public Response getResponse(int responseIndex) throws IOException {
                Response response = createMockedResponse(readFileFromResources("result/search_invalid_result.html"));
                Response networkResponse = createNetworkResponse("your-travel-options");
                PowerMockito.when(response.networkResponse()).thenReturn(networkResponse);
                return response;
            }
        });

        checkForErrorId(result, TaskJourneySearch.ERROR_INVALID_RESULTS);
    }

    private void checkForErrorId(AsyncResult result, int errorId) {
        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getErrorId(), errorId);
    }

    private Response createNetworkResponse(String location) throws IOException {
        Response networkResponse = createMockedResponse("");
        PowerMockito.when(networkResponse.header("location")).thenReturn(location);
        return networkResponse;
    }

    private AsyncResult<TaskJourneySearch.JourneyResponse> executeTask(HttpResponseHandler responseHandler) throws IOException {
        PreferencesProvider preferencesProvider = new PreferencesProvider() {
            @Override
            public Preferences getPreferences() {
                return new InMemoryPreferences();
            }
        };

        // We aren't testing the place parser functionality.
        PlaceParser placeParser = new PlaceParser(Mockito.mock(BusStopDao.class));

        // We don't need to worry about cookie persistance for these tests.
        CookieManagerFacade cookieManager = Mockito.mock(CookieManagerFacade.class, Mockito.RETURNS_MOCKS);

        Date time = new Date();

        JourneyCriteria journeyCriteria = new JourneyCriteria();
        journeyCriteria.setFromAddress("Capalaba");
        journeyCriteria.setToAddress("South Bank");
        journeyCriteria.setTime(time);
        journeyCriteria.setJourneyTimeCriteria(JourneyTimeCriteria.LeaveAfter);
        //journeyCriteria.setJourneyTransport(JourneyTransport.All);

        OkHttpClient client = mockHttpClient(responseHandler);

        TaskJourneySearch journeySearch = new TaskJourneySearch(null, client, preferencesProvider, placeParser, cookieManager);
        return journeySearch.execute(TaskJourneySearch.createParams(journeyCriteria, time, null));
    }

}
