package com.lach.translink.tasks.result;

import android.content.Context;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.ApplicationContext;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.log.Log;
import com.lach.common.util.RegexUtil;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyPreference;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.data.place.PlaceParser;
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
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class TaskJourneySearch implements Task<TaskJourneySearch.JourneyResponse> {
    private static final String TAG = "TaskJourneySearch";

    private static final String JOURNEY_SEARCH_DOMAIN = "http://mobile.jp.translink.com.au";

    public static final int ERROR_INVALID_CONTENT = 2000;
    public static final int ERROR_INVALID_DATE = 2001;
    public static final int ERROR_INVALID_LOCATION = 2002;
    public static final int ERROR_INVALID_RESULTS = 2003;

    private static final Pattern PATTERN_GENERIC_ERROR = Pattern.compile("there were errors", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_INVALID_DATE = Pattern.compile("date is invalid", Pattern.CASE_INSENSITIVE);

    private final Context context;
    private final OkHttpClient client;
    private final PreferencesProvider preferencesProvider;
    private final PlaceParser placeParser;
    private final CookieManagerFacade cookieManager;

    @Inject
    public TaskJourneySearch(@ApplicationContext Context context, OkHttpClient okHttpClient, PreferencesProvider preferencesProvider,
                             PlaceParser placeParser, CookieManagerFacade cookieManager) {
        this.context = context;
        this.client = okHttpClient;
        this.preferencesProvider = preferencesProvider;
        this.placeParser = placeParser;
        this.cookieManager = cookieManager;
    }

    public static Object[] createParams(JourneyCriteria journeyCriteria, Date date, String userAgent) {
        Object[] params = new Object[3];
        params[0] = journeyCriteria;
        params[1] = date;
        params[2] = userAgent;
        return params;
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

        cookieManager.init(context);
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        client.setCookieHandler(cookieManager);

        client.networkInterceptors().add(new UserAgentInterceptor(userAgent));

        // Prevent redirects, we will handle this manually.
        client.setFollowRedirects(false);

        return executeSearch(journeyCriteria, date, userAgent, null);
    }

    private AsyncResult<JourneyResponse> executeSearch(JourneyCriteria journeyCriteria, Date date, String userAgent,
                                                       RecoverableSearchErrors previousRecoverableErrors) throws Exception {

        FormEncodingBuilder formData = new FormEncodingBuilder();

        Preferences preferences = preferencesProvider.getPreferences();
        String walkMax = JourneyPreference.MAX_WALKING_DISTANCE.get(preferences);
        String walkSpeed = JourneyPreference.WALK_SPEED.get(preferences);

        //
        // Use the correct search text if the criteria was encoded.
        //
        String fromAddress = placeParser.getPlaceSearchText(journeyCriteria.getFromAddress(),
                previousRecoverableErrors != null && previousRecoverableErrors.badStart);

        String toAddress = placeParser.getPlaceSearchText(journeyCriteria.getToAddress(),
                previousRecoverableErrors != null && previousRecoverableErrors.badEnd);

        formData.add("Start", fromAddress);
        formData.add("End", toAddress);

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

        // Add all the appropriate transport values.
        List<JourneyTransport> transType = journeyCriteria.getJourneyTransport();
        if (transType != null) {
            boolean allTransport = transType.contains(JourneyTransport.All);
            if (allTransport || (transType.contains(JourneyTransport.Bus))) {
                formData.add("TransportModes", "Bus");
            }
            if (allTransport || (transType.contains(JourneyTransport.Train))) {
                formData.add("TransportModes", "Train");
            }
            if (allTransport || (transType.contains(JourneyTransport.Ferry))) {
                formData.add("TransportModes", "Ferry");
            }
            if (allTransport || (transType.contains(JourneyTransport.Tram))) {
                formData.add("TransportModes", "Tram");
            }
        }

        String serviceTypes = JourneyPreference.SERVICE_TYPES.get(preferences);
        String[] services = serviceTypes.split("~");

        for (String s : services) {
            formData.add("ServiceTypes", s);
        }

        String fareTypes = JourneyPreference.FARE_TYPES.get(preferences);
        String[] fares = fareTypes.split("~");

        for (String s : fares) {
            formData.add("FareTypes", s);
        }

        formData.add("MaximumWalkingDistance", walkMax);
        formData.add("WalkingSpeed", walkSpeed);

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
            if (PATTERN_INVALID_DATE.matcher(body).find()) {
                Log.warn(TAG, "Invalid date detected.");
                return new AsyncResult<>(ERROR_INVALID_DATE);
            }

            // Don't attempt to recover from the errors twice. This will take too long.
            if (previousRecoverableErrors == null) {
                RecoverableSearchErrors recoverableErrors = findRecoverableSearchErrors(body);

                // Run a new search to hopefully fix the issue.
                if (recoverableErrors != null) {
                    return executeSearch(journeyCriteria, date, userAgent, recoverableErrors);
                }
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
        if (body != null) {
            if (PATTERN_GENERIC_ERROR.matcher(body).find()) {
                return new AsyncResult<>(ERROR_INVALID_RESULTS);
            }
        }

        // If there were no errors raised, pass the response back.
        JourneyResponse journeyResponse = new JourneyResponse(JOURNEY_SEARCH_DOMAIN + location, journeyCriteria, body);
        return new AsyncResult<>(journeyResponse);
    }

    /**
     * Finds any errors which we could possibly fix when executing another search.
     *
     * @param body the html body which should contain the errors.
     * @return a recoverable errors structure if any errors can be recovered from. Otherwise null.
     */
    private RecoverableSearchErrors findRecoverableSearchErrors(String body) {
        //
        // See if we can figure out how to resolve the error.
        // Only do this on the first search attempt.
        //
        String[] summaryErrors = RegexUtil.findMatches("div class=\"validation-summary-errors\".*?</div>", body, true);
        if (summaryErrors.length > 0) {
            RecoverableSearchErrors recoverableSearchErrors = new RecoverableSearchErrors();

            // Search for errors pertaining to bad location data.
            String[] badLocationErrors = RegexUtil.findMatches("matched your (\\w+) location", summaryErrors[0], true);
            if (badLocationErrors.length > 0) {

                for (String locationError : badLocationErrors) {
                    if (!recoverableSearchErrors.isBadStart()) {
                        recoverableSearchErrors.setBadStart(locationError.equalsIgnoreCase("start"));
                    }
                    if (!recoverableSearchErrors.isBadEnd()) {
                        recoverableSearchErrors.setBadEnd(locationError.equalsIgnoreCase("end"));
                    }
                }
            }

            if (recoverableSearchErrors.isBadStart() || recoverableSearchErrors.isBadEnd()) {
                return recoverableSearchErrors;
            }
        }

        return null;
    }

    private String getBody(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            return responseBody.string();
        }
        return null;
    }

    private static class RecoverableSearchErrors {
        private boolean badStart = false;
        private boolean badEnd = false;

        public boolean isBadStart() {
            return badStart;
        }

        public void setBadStart(boolean badStart) {
            this.badStart = badStart;
        }

        public boolean isBadEnd() {
            return badEnd;
        }

        public void setBadEnd(boolean badEnd) {
            this.badEnd = badEnd;
        }
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
