package com.lach.translink.tasks.result;

import android.content.Context;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.ApplicationContext;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.log.Log;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.network.UserAgentInterceptor;
import com.lachlanm.xwalkfallback.CookieManagerFacade;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class TaskJourneySearch implements Task<TaskJourneySearch.JourneyResponse> {
    private static final String TAG = "TaskJourneySearch";

    private static final String JOURNEY_SEARCH_DOMAIN = "http://mobile.jp.translink.com.au";

    private static final String ERROR_TEXT_INVALID_DATE = "date is invalid";

    public static final int ERROR_INVALID_CONTENT = 2000;
    public static final int ERROR_INVALID_DATE = 2001;
    public static final int ERROR_INVALID_LOCATION = 2002;
    public static final int ERROR_INVALID_RESULTS = 2003;

    private final Context context;
    private final PreferencesProvider preferencesProvider;
    private final CookieManagerFacade cookieManager;

    @Inject
    public TaskJourneySearch(@ApplicationContext Context context, PreferencesProvider preferencesProvider, CookieManagerFacade cookieManager) {
        this.context = context;
        this.preferencesProvider = preferencesProvider;
        this.cookieManager = cookieManager;
    }

    @Override
    public AsyncResult<JourneyResponse> execute(Object... params) {
        AsyncResult<JourneyResponse> result;
        try {
            result = executeInternal(params);
        } catch (Exception ex) {
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    private AsyncResult<JourneyResponse> executeInternal(Object... params) throws Exception {
        JourneyCriteria journeyCriteria = (JourneyCriteria) params[0];
        Date date = (Date) params[1];
        String userAgent = (String) params[2];

        FormEncodingBuilder formData = new FormEncodingBuilder();

        Preferences preferences = preferencesProvider.getPreferences();
        String walkMax = preferences.getString("maxwalk", "1000");
        String walkSpeed = preferences.getString("walkspeed", "Normal");

        formData.add("Start", journeyCriteria.getFromAddress());
        formData.add("End", journeyCriteria.getToAddress());

        JourneyTimeCriteria timeCriteria = journeyCriteria.getJourneyTimeCriteria();
        if (timeCriteria.equals(JourneyTimeCriteria.ArriveBefore)) {
            formData.add("TimeSearchMode", "ArriveBefore");

        } else if (timeCriteria.equals(JourneyTimeCriteria.LeaveAfter)) {
            formData.add("TimeSearchMode", "LeaveAfter");

        } else if (timeCriteria.equals(JourneyTimeCriteria.FirstTrip)) {
            formData.add("TimeSearchMode", "FirstServices");

        } else {
            formData.add("TimeSearchMode", "LastServices");

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy");
        formData.add("SearchDate", dateFormat.format(date) + " 12:00:00 AM");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(journeyCriteria.getTime());

        int hour = calendar.get(Calendar.HOUR);
        if (hour == 0) {
            // Calendar time goes from 0 - 11
            hour = 12;
        }
        formData.add("SearchHour", "" + hour);
        formData.add("SearchMinute", "" + calendar.get(Calendar.MINUTE));
        formData.add("TimeMeridiem", ((calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM"));

        JourneyTransport transType = journeyCriteria.getJourneyTransport();
        if ((transType == JourneyTransport.All) || (transType == JourneyTransport.Bus)) {
            formData.add("TransportModes", "Bus");
        }
        if ((transType == JourneyTransport.All) || (transType == JourneyTransport.Train)) {
            formData.add("TransportModes", "Train");
        }
        if ((transType == JourneyTransport.All) || (transType == JourneyTransport.Ferry)) {
            formData.add("TransportModes", "Ferry");
        }
        if ((transType == JourneyTransport.All) || (transType == JourneyTransport.Tram)) {
            formData.add("TransportModes", "Tram");
        }

        String serviceTypes = preferences.getString("serviceTypes", "Regular~Express~NightLink");
        String[] services = serviceTypes.split("~");

        for (String s : services) {
            formData.add("ServiceTypes", s);
        }

        String fareTypes = preferences.getString("fareTypes", "Free~Standard~Prepaid");
        String[] fares = fareTypes.split("~");

        for (String s : fares) {
            formData.add("FareTypes", s);
        }

        formData.add("MaximumWalkingDistance", walkMax);
        formData.add("WalkingSpeed", walkSpeed);

        OkHttpClient client = new OkHttpClient();

        cookieManager.init(context);
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        client.setCookieHandler(cookieManager);

        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.networkInterceptors().add(new UserAgentInterceptor(userAgent));

        // Prevent redirects, we will handle this manually.
        client.setFollowRedirects(false);

        Request request = new Request.Builder()
                .url(JOURNEY_SEARCH_DOMAIN + "/travel-information/journey-planner")
                .post(formData.build())
                .build();

        Response response = client.newCall(request).execute();
        String location = response.networkResponse().header("location");

        if (location != null) {
            if (location.contains("your-travel-options")) {
                return handleTravelOptions(client, location, response, journeyCriteria);

            } else if (location.contains("confirm-location")) {
                // TODO: This is only a temporary work around until a better mechanism is built.
                Log.error(TAG, "JourneySearchInvalidLocation. location: " + location +
                        ", From: '" + journeyCriteria.getFromAddress() +
                        "', To: '" + journeyCriteria.getToAddress() + "'");

                return new AsyncResult<>(ERROR_INVALID_LOCATION);
            }
        }

        String body = getBody(response);
        if (body != null) {
            if (body.toLowerCase().contains(ERROR_TEXT_INVALID_DATE)) {
                Log.warn(TAG, "Invalid date detected.");
                return new AsyncResult<>(ERROR_INVALID_DATE);
            }
        }

        Log.error(TAG, "Invalid data. location: " + location + ", body: " + body);
        return new AsyncResult<>(ERROR_INVALID_CONTENT);
    }

    private AsyncResult<JourneyResponse> handleTravelOptions(OkHttpClient client, String location, Response response, JourneyCriteria journeyCriteria) throws IOException {
        // Redirect until we have finished, or cannot find a location to navigate to.
        Request request;
        while (response.isRedirect()) {

            location = response.header("location");
            if (location == null) {
                break;
            }

            request = new Request.Builder()
                    .url(JOURNEY_SEARCH_DOMAIN + location)
                    .build();
            response = client.newCall(request).execute();
        }

        String body = getBody(response);

        // Check if the page mentions any errors with the search criteria.
        if (body != null && body.toLowerCase().contains("there were errors")) {
            return new AsyncResult<>(ERROR_INVALID_RESULTS);
        }

        // If there were no errors raised, pass the response back.
        JourneyResponse journeyResponse = new JourneyResponse(JOURNEY_SEARCH_DOMAIN + location, journeyCriteria, body);
        return new AsyncResult<>(journeyResponse);
    }

    private String getBody(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            return responseBody.string();
        }
        return null;
    }

    public static class JourneyResponse {
        public final String url;
        public final JourneyCriteria criteria;
        public final String content;

        @SuppressWarnings("SameParameterValue")
        private JourneyResponse(String url, JourneyCriteria criteria, String content) {
            this.url = url;
            this.criteria = criteria;
            this.content = content;
        }
    }
}
