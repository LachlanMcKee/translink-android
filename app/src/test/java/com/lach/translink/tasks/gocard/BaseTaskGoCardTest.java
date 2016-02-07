package com.lach.translink.tasks.gocard;

import com.lach.translink.BaseTest;
import com.lach.translink.network.gocard.GoCardHttpClient;

import java.io.IOException;

public abstract class BaseTaskGoCardTest extends BaseTest {

    /**
     * Creates a mocked go-card http client with a hard-coded html response which is extracted from
     * the testing resources using the input file name.
     *
     * @param responseFileName the name of the file being read.
     * @return the mocked http client.
     * @throws IOException thrown if the input file cannot be read.
     */
    public GoCardHttpClient createMockedHttpClient(String responseFileName) throws IOException {
        final String response = readFileFromResources(responseFileName);
        return new GoCardHttpClient() {
            @Override
            public Response getResponseForUrl(String url) throws IOException {
                return new Response(true, response);
            }
        };
    }

}
