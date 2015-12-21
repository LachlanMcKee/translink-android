package com.lach.translink.activities;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.test.espresso.IdlingResource;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.lach.common.data.CoreModule;
import com.lach.common.data.preference.InMemoryPreferences;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.translink.TranslinkApplication;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.lach.translink.activities.espressso.matcher.ViewMatchersExt.invertMatcher;

public abstract class BaseTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
    // CoreModule
    protected InMemoryPreferences inMemoryPreferences;

    public BaseTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    public TranslinkApplication getApplication() {
        return (TranslinkApplication) getInstrumentation().getTargetContext().getApplicationContext();
    }

    public void init() {
        init(true);
    }

    public void init(boolean initActivity) {
        // CoreModule
        inMemoryPreferences = new InMemoryPreferences();

        postInit();

        if (initActivity) {
            getActivity();
        }
    }

    public void postInit() {

    }

    public void findViewWithText(@StringRes int stringResId) {
        findViewWithText(stringResId, true);
    }

    public void findViewWithText(String text) {
        findViewWithText(text, true);
    }

    public void findViewWithText(@StringRes int stringResId, boolean expectVisible) {
        Matcher<View> visibilityMatcher = invertMatcher(isDisplayed(), !expectVisible);
        onView(withText(stringResId)).check(matches(visibilityMatcher));
    }

    public void findViewWithText(String text, boolean expectVisible) {
        Matcher<View> visibilityMatcher = invertMatcher(isDisplayed(), !expectVisible);
        onView(withText(text)).check(matches(visibilityMatcher));
    }

    public CoreModule createCoreModule() {
        return new CoreModule(getApplication()) {
            @Override
            public PreferencesProvider providesPreferencesProvider() {
                return new PreferencesProvider() {
                    @Override
                    public Preferences getPreferences() {
                        return inMemoryPreferences;
                    }
                };
            }
        };
    }

    public class ElapsedTimeIdlingResource implements IdlingResource {
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
