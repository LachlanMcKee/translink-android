package com.lach.translink.tasks.gocard;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Instrumentation;
import com.lach.common.log.InstrumentationLogLevel;
import com.lach.common.log.Log;
import com.lach.common.util.RegexUtil;
import com.lach.translink.data.gocard.GoCardDetails;
import com.lach.translink.network.GoCardHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.inject.Inject;

public class TaskGoCardDetails implements Task<GoCardDetails> {
    private static final String TAG = "TaskGoCardDetails";

    public static final int ERROR_BAD_PARSING = 2000;
    public static final int ERROR_CONNECTION_TROUBLE = 2001;
    public static final int ERROR_INVALID_CREDENTIALS = 2002;

    private final GoCardHttpClient client;

    @Inject
    public TaskGoCardDetails(GoCardHttpClient client) {
        this.client = client;
    }

    @Override
    public AsyncResult<GoCardDetails> execute(Object... params) {
        AsyncResult<GoCardDetails> result;
        try {
            result = executeInternal();

            if (result.getErrorId() == ERROR_CONNECTION_TROUBLE) {
                Instrumentation.logEvent("GoCardInfoConnectionException", InstrumentationLogLevel.Error);
            }

        } catch (Exception ex) {
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    public AsyncResult<GoCardDetails> executeInternal() throws Exception {
        client.init();

        Request request = new Request.Builder()
                .url("https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/summary")
                .build();

        Response response;
        String responseBody;
        try {
            response = client.newCall(request).execute();
            responseBody = response.body().string();

        } catch (IOException ex) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }

        if (!response.isSuccessful()) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }
        if (responseBody.contains("There was a problem")) {
            Instrumentation.logEventWithData("GoCardInfoCredentialsException", InstrumentationLogLevel.Warning, "HTML", responseBody);
            return new AsyncResult<>(ERROR_INVALID_CREDENTIALS);
        }
        if (responseBody.contains("website is currently unavailable")) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }

        String issueRegex = "Issued:</strong>(.*?)</div>";
        String expireRegex = "Expires:</strong>(.*?)</div>";
        String balanceRegex = "Card balance</th>.*?<td>.*?<td>(.*?)</td>";
        String balanceTimeRegex = "Card balance</th>.*?<td>(.*?)</td>";
        String passengerTypeRegex = "Type:</strong>(.*?)</div>";

        GoCardDetails details = new GoCardDetails();
        try {
            String[] issueDates = RegexUtil.findMatches(issueRegex, responseBody, true);
            String[] expireDates = RegexUtil.findMatches(expireRegex, responseBody, true);
            String[] balances = RegexUtil.findMatches(balanceRegex, responseBody, true);
            String[] balanceTimes = RegexUtil.findMatches(balanceTimeRegex, responseBody, true);
            String[] passengerTypes = RegexUtil.findMatches(passengerTypeRegex, responseBody, true);

            details.issueDate = issueDates[0].trim();
            details.expiryDate = expireDates[0].trim();
            details.balance = balances[0];
            details.balanceTime = balanceTimes[0].trim();
            details.passengerType = passengerTypes[0];

            return new AsyncResult<>(details);
        } catch (Exception ex) {
            Log.error(TAG, "There was an error trying to read the website data.", ex, "HTML", responseBody);
            return new AsyncResult<>(ERROR_BAD_PARSING);
        }
    }

}
