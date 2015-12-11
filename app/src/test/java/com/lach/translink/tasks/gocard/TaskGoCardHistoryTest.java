package com.lach.translink.tasks.gocard;

import com.lach.common.async.AsyncResult;
import com.lach.translink.data.gocard.GoCardTransaction;

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
        GoCardTransaction titleTransaction = item.get(0);
        Assert.assertEquals(titleTransaction.date, "10 July 2015");
        Assert.assertNull(titleTransaction.startTime);
        Assert.assertNull(titleTransaction.endTime);
        Assert.assertNull(titleTransaction.fromLocation);
        Assert.assertNull(titleTransaction.toLocation);
        Assert.assertNull(titleTransaction.amount);
        Assert.assertFalse(titleTransaction.isTopUp);

        // Testing a 'journey' element.
        GoCardTransaction journeyTransaction = item.get(1);
        Assert.assertNull(journeyTransaction.date);
        Assert.assertEquals(journeyTransaction.startTime, "03:00 PM");
        Assert.assertEquals(journeyTransaction.endTime, "03:50 PM");
        Assert.assertEquals(journeyTransaction.fromLocation, "South Bank 1 ferry terminal");
        Assert.assertEquals(journeyTransaction.toLocation, "Bretts Wharf ferry terminal");
        Assert.assertEquals(journeyTransaction.amount, "$ 3.00");
        Assert.assertFalse(journeyTransaction.isTopUp);

        // Testing a 'top-up' element.
        GoCardTransaction topUpTransaction = item.get(2);
        Assert.assertNull(topUpTransaction.date);
        Assert.assertEquals(topUpTransaction.startTime, "12:00 PM");
        Assert.assertEquals(topUpTransaction.endTime, "");
        Assert.assertEquals(topUpTransaction.fromLocation, "Top up/Adjustment  ( North Quay 1 ferry terminal  )");
        Assert.assertEquals(topUpTransaction.toLocation, "$ 20.00");
        Assert.assertEquals(topUpTransaction.amount, "$ 23.00");
        Assert.assertTrue(topUpTransaction.isTopUp);
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
