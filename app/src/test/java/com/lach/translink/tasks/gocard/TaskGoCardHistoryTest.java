package com.lach.translink.tasks.gocard;

import com.lach.common.async.AsyncResult;
import com.lach.translink.data.gocard.GoCardTransaction;
import com.lach.translink.data.gocard.GoCardTransactionGroup;
import com.lach.translink.data.gocard.GoCardTransactionJourney;
import com.lach.translink.data.gocard.GoCardTransactionTopUp;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class TaskGoCardHistoryTest extends BaseTaskGoCardTest {

    @Test
    public void testHistorySuccess() throws IOException {
        AsyncResult<ArrayList<GoCardTransaction>> result = executeMockedGoCardHistoryTask("gocard/history/gocard_history_success.html");
        Assert.assertFalse(result.hasError());

        // These correspond to the values in the input file.
        ArrayList<GoCardTransaction> item = result.getItem();
        Assert.assertEquals(item.size(), 3);

        // Testing a 'title' element.
        GoCardTransactionGroup titleTransaction = (GoCardTransactionGroup) item.get(0);
        Assert.assertEquals(titleTransaction.title, "10 July 2015");

        // Testing a 'journey' element.
        GoCardTransactionJourney journeyTransaction = (GoCardTransactionJourney) item.get(1);
        Assert.assertEquals(journeyTransaction.startTime, "03:00 PM");
        Assert.assertEquals(journeyTransaction.endTime, "03:50 PM");
        Assert.assertEquals(journeyTransaction.fromLocation, "South Bank 1 ferry terminal");
        Assert.assertEquals(journeyTransaction.toLocation, "Bretts Wharf ferry terminal");
        Assert.assertEquals(journeyTransaction.amount, "$ 3.00");

        // Testing a 'top-up' element.
        GoCardTransactionTopUp topUpTransaction = (GoCardTransactionTopUp) item.get(2);
        Assert.assertEquals(topUpTransaction.time, "12:00 PM");
        Assert.assertEquals(topUpTransaction.description, "Top up/Adjustment  ( North Quay 1 ferry terminal  )");
        Assert.assertEquals(topUpTransaction.oldAmount, "$ 20.00");
        Assert.assertEquals(topUpTransaction.newAmount, "$ 23.00");
    }

    /**
     * Creates a mocked go-card history task with a hard-coded html response which is extracted from
     * the testing resources using the input file name.
     *
     * @param responseFileName the name of the file being read.
     * @return the result of the mocked task.
     * @throws IOException thrown if the input file cannot be read.
     */
    private AsyncResult<ArrayList<GoCardTransaction>> executeMockedGoCardHistoryTask(String responseFileName) throws IOException {
        TaskGoCardHistory taskGoCardHistory = new TaskGoCardHistory(createMockedHttpClient(responseFileName));
        taskGoCardHistory.setEndDate(new Date());
        return taskGoCardHistory.execute();
    }

}
