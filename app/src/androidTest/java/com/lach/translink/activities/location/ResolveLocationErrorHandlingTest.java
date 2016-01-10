package com.lach.translink.activities.location;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.CountingIdlingResource;
import android.support.test.runner.AndroidJUnit4;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.translink.activities.R;
import com.lach.translink.ui.impl.resolve.ResolveLocationActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(AndroidJUnit4.class)
public class ResolveLocationErrorHandlingTest extends BaseResolveLocationTestCase {
    private static final String TAG = ResolveLocationErrorHandlingTest.class.getSimpleName();

    // Unfortunately due to the autocomplete message handler, we must add a delay.
    CountingIdlingResource countingIdlingResource;

    @Test
    public void testFindLocationFailureHandling() throws Exception {
        init(false);

        // Unfortunately due to the autocomplete message handler, we must add a delay.
        countingIdlingResource = new CountingIdlingResource("Message queue");

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                countingIdlingResource.decrement();
                return new AsyncResult(TaskGenericErrorType.IO_FAILURE);
            }
        }).when(taskFindLocation).execute(Mockito.anyVararg());

        // Start the activity.
        getActivity();

        changeCriteria("Wyn", true);
        findViewWithText(R.string.resolve_searching_for_location);

        countingIdlingResource.increment();
        Espresso.registerIdlingResources(countingIdlingResource);

        // Ensure that the networking error text is displayed.
        findViewWithText(R.string.resolve_generic_error);

        // Clean up
        Espresso.unregisterIdlingResources(countingIdlingResource);
    }

    @Test
    public void testGetAddressFailureHandling() throws InterruptedException {
        init(false);

        Mockito.doReturn(new AsyncResult(TaskGenericErrorType.INVALID_NETWORK_RESPONSE))
                .when(taskGetAddress)
                .execute(Mockito.anyString());

        // Start the activity.
        getActivity();

        // We are expecting an error dialog.
        selectMapPin(false);
        findViewWithText(R.string.resolve_generic_error);
    }

    @Override
    public Class<ResolveLocationActivity> getTestActivityType() {
        return ResolveLocationActivity.class;
    }
}
