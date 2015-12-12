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

    private AsyncResult<GoCardDetails> executeInternal() {
        GoCardHttpClient.Response response;
        try {
            response = client.getResponseForUrl("https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/summary");

        } catch (IOException ex) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }

        if (!response.isSuccess) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }
        
        String content = response.content;
        if (content.contains("There was a problem")) {
            Instrumentation.logEventWithData("GoCardInfoCredentialsException", InstrumentationLogLevel.Warning, "HTML", content);
            return new AsyncResult<>(ERROR_INVALID_CREDENTIALS);
        }
        if (content.contains("website is currently unavailable")) {
            return new AsyncResult<>(ERROR_CONNECTION_TROUBLE);
        }

        String issueRegex = "Issued:</strong>(.*?)</div>";
        String expireRegex = "Expires:</strong>(.*?)</div>";
        String balanceRegex = "Card balance</th>.*?<td>.*?<td>(.*?)</td>";
        String balanceTimeRegex = "Card balance</th>.*?<td>(.*?)</td>";
        String passengerTypeRegex = "Type:</strong>(.*?)</div>";

        GoCardDetails details = new GoCardDetails();
        try {
            String[] issueDates = RegexUtil.findMatches(issueRegex, content, true);
            String[] expireDates = RegexUtil.findMatches(expireRegex, content, true);
            String[] balances = RegexUtil.findMatches(balanceRegex, content, true);
            String[] balanceTimes = RegexUtil.findMatches(balanceTimeRegex, content, true);
            String[] passengerTypes = RegexUtil.findMatches(passengerTypeRegex, content, true);

            details.issueDate = issueDates[0].trim();
            details.expiryDate = expireDates[0].trim();
            details.balance = balances[0];
            details.balanceTime = balanceTimes[0].trim();
            details.passengerType = passengerTypes[0].trim();

            return new AsyncResult<>(details);
        } catch (Exception ex) {
            Log.error(TAG, "There was an error trying to read the website data.", ex);
            return new AsyncResult<>(ERROR_BAD_PARSING);
        }
    }

}
