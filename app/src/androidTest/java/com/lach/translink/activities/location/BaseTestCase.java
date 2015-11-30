package com.lach.translink.activities.location;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.test.espresso.IdlingResource;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.lach.translink.TranslinkApplication;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.lach.translink.activities.espressso.matcher.ViewMatchersExt.invertMatcher;


public abstract class BaseTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
    public BaseTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    TranslinkApplication getApplication() {
        return (TranslinkApplication) getInstrumentation().getTargetContext().getApplicationContext();
    }

    void init() {
        init(true);
    }

    void init(boolean initActivity) {
        postInit();

        if (initActivity) {
            getActivity();
        }
    }

    void postInit() {

    }

    void findViewWithText(@StringRes int stringResId) {
        findViewWithText(stringResId, true);
    }

    void findViewWithText(String text) {
        findViewWithText(text, true);
    }

    void findViewWithText(@StringRes int stringResId, boolean expectVisible) {
        Matcher<View> visibilityMatcher = invertMatcher(isDisplayed(), !expectVisible);
        onView(withText(stringResId)).check(matches(visibilityMatcher));
    }

    void findViewWithText(String text, boolean expectVisible) {
        Matcher<View> visibilityMatcher = invertMatcher(isDisplayed(), !expectVisible);
        onView(withText(text)).check(matches(visibilityMatcher));
    }

    class ElapsedTimeIdlingResource implements IdlingResource {
        private final long startTime;
        private final long waitingTime;
        private ResourceCallback resourceCallback;

        public ElapsedTimeIdlingResource(long waitingTime) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = waitingTime;
        }

        @Override
        public String getName() {
            return ElapsedTimeIdlingResource.class.getName() + ":" + waitingTime;
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = (elapsed >= waitingTime);
            if (idle) {
                resourceCallback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

}
