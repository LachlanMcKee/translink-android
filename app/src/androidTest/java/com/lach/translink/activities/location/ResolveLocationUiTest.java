package com.lach.translink.activities.location;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.lach.common.async.AsyncResult;
import com.lach.translink.activities.R;
import com.lach.translink.activities.espressso.action.ViewActionsExt;
import com.lach.translink.ui.impl.resolve.ResolveLocationActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ResolveLocationUiTest extends BaseResolveLocationTestCase {

    private static final String DUMMY_ADDRESS = "Dummy address";

    @Test
    public void testSearchCriteriaValidation() {
        init();

        //
        // The validation rules are as follows
        // - the string must have at least three alphabet characters
        //
        changeCriteria("Wy", false);
        changeCriteria("Wyn", true);
        changeCriteria("W n", false);
        changeCriteria("W n-123", false);

        //
        // Special validation rules for stop ids are as follows
        // - the string must have at least three numeric characters and at most six.
        //
        changeCriteria("00", false);
        changeCriteria("001", true);
        changeCriteria("001234", true);
        changeCriteria("0012345", false);
    }

    @Test
    public void testSearchCriteriaPersistence() {
        init();

        final String criteria = "Wy";
        changeCriteria(criteria, false);

        ViewAssertion textMatcher = matches(withText(criteria));

        // Check if the text survives both orientation changes.
        onView(isRoot()).perform(ViewActionsExt.orientationLandscape());
        onView(isAssignableFrom(EditText.class)).check(textMatcher);

        onView(isRoot()).perform(ViewActionsExt.orientationPortrait());
        onView(isAssignableFrom(EditText.class)).check(textMatcher);
    }

    @Test
    public void testContacts() {
        init(false);

        Mockito.when(contactAddressExtractor.getContactsAddress(Mockito.any(Context.class), Mockito.any(Uri.class)))
                .thenReturn(DUMMY_ADDRESS);

        getActivity();

        // Mock the intent which is going to the contacts screen.
        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Set up result stubbing when an intent sent to "contacts" is seen.
        Intents.init();
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_PICK)).respondWith(result);

        // Click the contacts button.
        onView(ViewMatchers.withText(R.string.resolve_search_contacts)).perform(click());

        // Assert that address we set up above is shown.
        onView(isAssignableFrom(EditText.class)).check(matches(withText(DUMMY_ADDRESS)));
    }

    @Test
    public void testMapIntegration() throws InterruptedException {
        init(false);

        List<String> addressList = new ArrayList<>(1);
        addressList.add(DUMMY_ADDRESS);

        Mockito.doReturn(new AsyncResult<>(addressList))
                .when(taskGetAddress)
                .execute(Mockito.anyVararg());

        // Start the activity.
        getActivity();

        selectMapPin(true);
    }

    @Override
    public Class<ResolveLocationActivity> getTestActivityType() {
        return ResolveLocationActivity.class;
    }
}
