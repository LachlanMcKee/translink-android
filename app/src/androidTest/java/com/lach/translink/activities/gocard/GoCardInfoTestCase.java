package com.lach.translink.activities.gocard;

import android.app.Activity;
import android.support.test.runner.AndroidJUnit4;

import com.lach.common.async.AsyncResult;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.translink.activities.BaseTestCase;
import com.lach.translink.activities.R;
import com.lach.translink.activities.TaskTester;
import com.lach.translink.data.gocard.GoCardDetails;
import com.lach.translink.network.NetworkInterceptorsModule;
import com.lach.translink.network.NetworkModule;
import com.lach.translink.network.gocard.DaggerGoCardNetworkComponent;
import com.lach.translink.network.gocard.GoCardCredentials;
import com.lach.translink.network.gocard.GoCardHttpClient;
import com.lach.translink.network.gocard.GoCardNetworkComponent;
import com.lach.translink.network.gocard.GoCardNetworkModule;
import com.lach.translink.tasks.gocard.TaskGoCardDetails;
import com.lach.translink.tasks.gocard.TaskGoCardHistory;
import com.lach.translink.ui.impl.gocard.GoCardInfoActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class GoCardInfoTestCase extends BaseTestCase<GoCardInfoActivity> {
    private GoCardCredentials goCardCredentials;
    private TaskGoCardDetails taskGoCardDetails;
    private TaskGoCardHistory taskGoCardHistory;

    private GoCardDetails mockedGoCardDetails;

    @Override
    public void postInit() {
        super.postInit();

        goCardCredentials = Mockito.mock(GoCardCredentials.class);
        taskGoCardDetails = Mockito.spy(new TaskGoCardDetails(null));
        taskGoCardHistory = Mockito.spy(new TaskGoCardHistory(null));

        mockedGoCardDetails = new GoCardDetails();
        mockedGoCardDetails.balance = "$20.00";
        mockedGoCardDetails.balanceTime = "1 Jul 2015 12:00 PM";
        mockedGoCardDetails.issueDate = "1 Jan 2015";
        mockedGoCardDetails.expiryDate = "1 Jan 2025";
        mockedGoCardDetails.passengerType = "Adult";

        // Inject the gocard component into the app being tested.
        getApplication().setGoCardNetworkComponentProvider(new Provider<GoCardNetworkComponent>() {
            @Override
            public GoCardNetworkComponent get() {
                return createGoCardNetworkComponent();
            }
        });
    }

    @Override
    public Class<GoCardInfoActivity> getTestActivityType() {
        return GoCardInfoActivity.class;
    }

    @Test
    public void testNoCredentials() {
        init();

        // Since we do not mock any credentials, we expect to see an error dialog.
        findViewWithText(R.string.gocard_info_bad_credentials_title);
    }

    @Test
    public void testDetails() {
        init(false);
        mockValidCredentials();

        TaskTester initialLoadDialogTester = new TaskTester(asyncTaskRunner, activityRule) {
            @Override
            public void preTask(Activity activity) {
                // Verify the refreshing message is displayed.
                findViewWithText("Loading Go-Card information");
            }

            @Override
            public void postTask(Activity activity) {
                // Since we do have mocked credentials, we should not see an error dialog.
                onView(withText(R.string.gocard_info_bad_credentials_title)).check(doesNotExist());

                // Ensure all the mocked content has been displayed on the screen.
                findViewWithText(mockedGoCardDetails.balance);
                findViewWithText(activity.getString(R.string.gocard_info_balance_as_of, mockedGoCardDetails.balanceTime));
                findViewWithText(mockedGoCardDetails.issueDate);
                findViewWithText(mockedGoCardDetails.expiryDate);
                findViewWithText(mockedGoCardDetails.passengerType);
            }
        };

        initialLoadDialogTester.setup();
        initialLoadDialogTester.execute(getActivity());
    }

    @Test
    public void testHistorySearchDialog() {
        init(false);
        mockValidCredentials();

        // Start the activity.
        GoCardInfoActivity activity = getActivity();

        TaskTester loadHistoryDialogTester = new TaskTester(asyncTaskRunner, activityRule) {
            @Override
            public void preTask(Activity activity) {
                // Verify the refreshing message is displayed.
                findViewWithText("Loading Go-Card history");
            }

            @Override
            public void postTask(Activity activity) {
            }
        };

        // Setup the plumbing for the task tester which handles testing the ui before the task executes.
        loadHistoryDialogTester.setup();

        onView(withText(R.string.gocard_info_load_history)).perform(click());

        loadHistoryDialogTester.execute(activity);
    }

    private GoCardNetworkComponent createGoCardNetworkComponent() {
        return DaggerGoCardNetworkComponent.builder()
                .coreModule(getCoreModule())
                .networkModule(new NetworkModule())
                .networkInterceptorsModule(new NetworkInterceptorsModule() {
                    @Override
                    public List<Interceptor> providesNetworkInterceptors() {
                        return new ArrayList<>();
                    }
                })
                .goCardNetworkModule(new GoCardNetworkModule() {
                    @Override
                    public GoCardCredentials providesGoCardCredentials(PreferencesProvider preferencesProvider) {
                        return goCardCredentials;
                    }

                    @Override
                    public GoCardHttpClient providesGoCardHttpClient(OkHttpClient.Builder httpClientBuilder, GoCardCredentials goCardCredentials) {
                        return new GoCardHttpClient() {
                            @Override
                            public Response getResponseForUrl(String url) throws IOException {
                                // We will mock the tasks directly, which means the go-card client won't be used.
                                return null;
                            }
                        };
                    }

                    @Override
                    public TaskGoCardDetails providesTaskGoCardDetails(GoCardHttpClient goCardHttpClient) {
                        return taskGoCardDetails;
                    }

                    @Override
                    public TaskGoCardHistory providesTaskGoCardHistory(GoCardHttpClient goCardHttpClient) {
                        return taskGoCardHistory;
                    }
                })
                .build();
    }

    private void mockValidCredentials() {
        Mockito.when(goCardCredentials.credentialsExist()).thenReturn(true);

        Mockito.doReturn(new AsyncResult<>(mockedGoCardDetails))
                .when(taskGoCardDetails)
                .execute(Mockito.anyVararg());
    }

}
