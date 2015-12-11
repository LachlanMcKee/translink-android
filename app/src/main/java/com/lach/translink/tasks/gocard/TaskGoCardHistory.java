package com.lach.translink.tasks.gocard;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.util.HtmlUtil;
import com.lach.common.util.RegexUtil;
import com.lach.translink.data.gocard.GoCardTransaction;
import com.lach.translink.network.GoCardHttpClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

public class TaskGoCardHistory implements Task<ArrayList<GoCardTransaction>> {
    public static final int HISTORY_MONTHS = 3;

    private final GoCardHttpClient client;
    private Date endDate;

    @Inject
    public TaskGoCardHistory(GoCardHttpClient client) {
        this.client = client;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public AsyncResult<ArrayList<GoCardTransaction>> execute(Object... params) {
        AsyncResult<ArrayList<GoCardTransaction>> result;
        try {
            result = executeInternal();
        } catch (Exception ex) {
            result = new AsyncResult<>(TaskGenericErrorType.findGenericErrorTypeByException(ex.getClass()));
        }
        return result;
    }

    private AsyncResult<ArrayList<GoCardTransaction>> executeInternal() throws Exception {
        String DATE_FORMAT_NOW = "dd/MM/yyyy";

        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        SimpleDateFormat serviceDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW, Locale.ENGLISH);

        String endDateText = serviceDateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -HISTORY_MONTHS);
        String startDateText = serviceDateFormat.format(cal.getTime());

        String url = "https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/history?startDate=%s&endDate=%s";
        GoCardHttpClient.Response response;

        try {
            response = client.getResponseForUrl(String.format(url, startDateText, endDateText));

        } catch (IOException ex) {
            return new AsyncResult<>(TaskGoCardDetails.ERROR_CONNECTION_TROUBLE);
        }

        if (!response.isSuccess) {
            return new AsyncResult<>(TaskGoCardDetails.ERROR_CONNECTION_TROUBLE);
        }

        String tableRowsRegex = "<tr.*?>(.*?)</tr>";
        String columnsRegex = "<td.*?>(.*?)</td>";

        String[] tableRows = RegexUtil.findMatches(tableRowsRegex, response.content, true);

        String currentDate = "";
        boolean newDateHandled = false;

        SimpleDateFormat resultsDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);

        ArrayList<GoCardTransaction> history = new ArrayList<>();
        for (String r : tableRows) {
            String[] columns = RegexUtil.findMatches(columnsRegex, r, true);

            if (columns.length > 4) {

                if (!newDateHandled) {
                    newDateHandled = true;

                    GoCardTransaction transaction = new GoCardTransaction();
                    transaction.date = currentDate;
                    history.add(transaction);
                }

                GoCardTransaction transaction = new GoCardTransaction();
                transaction.startTime = formatValue(columns[0]);
                transaction.fromLocation = formatValue(columns[1]);
                transaction.endTime = formatValue(columns[2]);
                transaction.toLocation = formatValue(columns[3]);
                transaction.amount = "$" + formatValue(columns[4]).replace("$", "");

                transaction.isTopUp = (r.contains("Top up"));

                history.add(transaction);
            } else if (columns.length >= 1) {

                String cellData = formatValue(columns[0]);

                if (!cellData.equals("Transactions")) {

                    // Attempt to change the date format, otherwise use the actual cell data.
                    try {
                        Date transactionDate = resultsDateFormat.parse(cellData);
                        currentDate = desiredDateFormat.format(transactionDate);
                    } catch (ParseException ignored) {
                        currentDate = cellData;
                    }

                    newDateHandled = false;
                }

            }
        }

        return new AsyncResult<>(history);
    }

    private String formatValue(String goCardValue) {
        goCardValue = goCardValue.replace("\r\n", "");

        // We handle the space ourselves to prevent the decoding from adding an actual no-breaking space.
        goCardValue = goCardValue.replace("&nbsp;", " ");
        goCardValue = HtmlUtil.decode(goCardValue);

        return goCardValue.trim();
    }

}
