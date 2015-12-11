package com.lach.translink.tasks.gocard;

import com.lach.common.async.AsyncResult;
import com.lach.translink.data.gocard.GoCardDetails;
import com.lach.translink.network.GoCardHttpClient;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaskGoCardDetailsTest extends BaseTaskGoCardTest {

    @Test
    public void testDetailSuccess() throws IOException {
        AsyncResult<GoCardDetails> result = executeMockedGoCardDetailsTask("gocard/details/gocard_details_success.html");
        Assert.assertFalse(result.hasError());

        // These correspond to the values in the input file.
        GoCardDetails item = result.getItem();
        Assert.assertEquals(item.issueDate, "1 Jan 2015");
        Assert.assertEquals(item.expiryDate, "1 Jan 2025");
        Assert.assertEquals(item.balance, "$20.00");
        Assert.assertEquals(item.balanceTime, "1 Jul 2015 12:00 PM");
        Assert.assertEquals(item.passengerType, "Adult");
    }

    @Test
    public void testDetailConnectionProblem() throws IOException {
        // Test when the http client throws any exceptions.
        AsyncResult<GoCardDetails> connectionErrorResult = new TaskGoCardDetails(new GoCardHttpClient() {
            @Override
            public Response getResponseForUrl(String url) throws IOException {
                throw new IOException();
            }
        }).execute();
        Assert.assertTrue(connectionErrorResult.hasError());

        // Test when the http client fails to find a 200 - success response.
        AsyncResult<GoCardDetails> responseInvalidResult = new TaskGoCardDetails(new GoCardHttpClient() {
            @Override
            public Response getResponseForUrl(String url) throws IOException {
                return new Response(false, null);
            }
        }).execute();
        Assert.assertTrue(responseInvalidResult.hasError());
    }

    @Test
    public void testDetailBadCredentials() throws IOException {
        AsyncResult<GoCardDetails> result = executeMockedGoCardDetailsTask("gocard/details/gocard_details_bad_credentials.html");
        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getErrorId(), TaskGoCardDetails.ERROR_INVALID_CREDENTIALS);
    }

    @Test
    public void testDetailWebsiteUnavailable() throws IOException {
        AsyncResult<GoCardDetails> result = executeMockedGoCardDetailsTask("gocard/details/gocard_details_website_unavailable.html");
        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getErrorId(), TaskGoCardDetails.ERROR_CONNECTION_TROUBLE);
    }

    @Test
    public void testDetailBadParse() throws IOException {
        AsyncResult<GoCardDetails> result = executeMockedGoCardDetailsTask("gocard/details/gocard_details_invalid_format.html");
        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getErrorId(), TaskGoCardDetails.ERROR_BAD_PARSING);
    }

    /**
     * Creates a mocked go-card details task with a hard-coded html response which is extracted from
     * the testing resources using the input file name.
     *
     * @param responseFileName the name of the file being read.
     * @return the result of the mocked task.
     * @throws IOException thrown if the input file cannot be read.
     */
    private AsyncResult<GoCardDetails> executeMockedGoCardDetailsTask(String responseFileName) throws IOException {
        TaskGoCardDetails taskGoCardDetails = new TaskGoCardDetails(createMockedHttpClient(responseFileName));
        return taskGoCardDetails.execute();
    }

}
