package com.lach.translink.activities.location;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.EditText;

import com.lach.common.async.AsyncResult;
import com.lach.translink.activities.R;
import com.lach.translink.activities.espressso.action.ViewActionsExt;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
public class ResolveLocationUiTest extends ResolveLocationTestCase {

    private static final String DUMMY_ADDRESS = "Dummy address";

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
    }

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

    public void testMapIntegration() throws InterruptedException {
        init(false);

        List<Address> addressList = new ArrayList<>(1);
        Address address = new Address(Locale.getDefault());
        address.setAddressLine(0, DUMMY_ADDRESS);
        addressList.add(address);

        Mockito.doReturn(new AsyncResult<>(addressList))
                .when(taskGetAddress)
                .execute(Mockito.anyVararg());

        // Start the activity.
        getActivity();

        selectMapPin(true);
    }

}
