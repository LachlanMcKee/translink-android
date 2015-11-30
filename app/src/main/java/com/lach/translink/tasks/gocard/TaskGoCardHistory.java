package com.lach.translink.tasks.gocard;

import android.text.Html;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.util.RegexUtil;
import com.lach.translink.data.gocard.GoCardTransaction;
import com.lach.translink.network.GoCardHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

    public AsyncResult<ArrayList<GoCardTransaction>> executeInternal() throws Exception {
        String DATE_FORMAT_NOW = "dd/MM/yyyy";

        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        SimpleDateFormat serviceDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW, Locale.ENGLISH);

        String endDateText = serviceDateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -HISTORY_MONTHS);
        String startDateText = serviceDateFormat.format(cal.getTime());

        client.init();

        String url = "https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/history?startDate=%s&endDate=%s";
        Request request = new Request.Builder()
                .url(String.format(url, startDateText, endDateText))
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        String tableRowsRegex = "<tr.*?>(.*?)</tr>";
        String columnsRegex = "<td.*?>(.*?)</td>";

        String[] tableRows = RegexUtil.findMatches(tableRowsRegex, responseBody, true);

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

        // We handle the space ourselves to prevent the html lib from adding an actual no-breaking space.
        goCardValue = goCardValue.replace("&nbsp;", " ");
        goCardValue = Html.fromHtml(goCardValue).toString();

        return goCardValue.trim();
    }

}
