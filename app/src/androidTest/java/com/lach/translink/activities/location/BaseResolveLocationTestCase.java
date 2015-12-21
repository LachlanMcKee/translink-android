package com.lach.translink.activities.location;

import android.support.annotation.CallSuper;
import android.support.test.espresso.action.ViewActions;
import android.view.View;
import android.widget.EditText;

import com.lach.common.log.Log;
import com.lach.translink.activities.BaseDataComponentTestCase;
import com.lach.translink.activities.BaseTestCase;
import com.lach.translink.activities.R;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.ui.impl.resolve.ResolveLocationActivity;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.lach.translink.activities.espressso.matcher.ViewMatchersExt.withSafeContentDescription;

public abstract class BaseResolveLocationTestCase extends BaseDataComponentTestCase<ResolveLocationActivity> {
    private static final String TAG = BaseResolveLocationTestCase.class.getSimpleName();

    public BaseResolveLocationTestCase() {
        super(ResolveLocationActivity.class);
    }

    @CallSuper
    @Override
    public void postInit() {
        super.postInit();
        setActivityIntent(ResolveLocationActivity.createIntent(getApplication(), PlaceType.FROM));
    }

    static void changeCriteria(BaseTestCase testCase, String text, boolean expectValid) {
        Log.debug(TAG, "changeCriteria. text: " + text + ", expectValid: " + expectValid);

        onView(isAssignableFrom(EditText.class)).perform(click(), ViewActions.replaceText(text));

        // If the resolve_choose_on_map button is found, the search hasn't executed.
        testCase.findViewWithText(R.string.resolve_choose_on_map, !expectValid);
    }

    void changeCriteria(String text, boolean expectValid) {
        changeCriteria(this, text, expectValid);
    }

    void openMapFragment() {
        // Click the map button.
        onView(withText(R.string.resolve_choose_on_map)).perform(click());
    }

    void selectMapPin(boolean expectSuccess) throws InterruptedException {
        // Click the map button.
        openMapFragment();

        // Check if we are now on the map activity
        onView(withText(R.string.resolve_map_subtitle_intro)).check(matches(isDisplayed()));

        // Click the centre of the map.
        onView(withContentDescription("Google Map")).perform(click());

        // Unfortunately this is required to test on the Support Map Fragment.
        Thread.sleep(1000);

        // Check if the confirm text and button are visible.
        Matcher<View> confirmButtonMatcher = withSafeContentDescription(R.string.resolve_confirm_button_description);
        onView(confirmButtonMatcher).check(matches(isDisplayed()));
        onView(withText(R.string.resolve_map_subtitle_continue)).check(matches(isDisplayed()));

        // Click the confirm button
        onView(confirmButtonMatcher).perform(click());

        if (expectSuccess) {
            // Ensure we are back on the list screen
            onView(withSafeContentDescription(R.string.resolve_back_button_description)).check(matches(isDisplayed()));
        }
    }

}
