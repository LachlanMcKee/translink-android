package com.lach.translink.activities.espressso.matcher;

import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.not;

public class ViewMatchersExt {

    /**
     * A safer way to check content description considering the default solution throws NPEs.
     *
     * @param resourceId the content description res-id
     * @return whether the view has a matching content description.
     */
    public static Matcher<View> withSafeContentDescription(@StringRes final int resourceId) {
        return new TypeSafeMatcher<View>() {
            private String resourceName = null;
            private String expectedText = null;

            public void describeTo(Description description) {
                description.appendText("with content description from resource id: ");
                description.appendValue(resourceId);
                if (null != this.resourceName) {
                    description.appendText("[");
                    description.appendText(this.resourceName);
                    description.appendText("]");
                }

                if (null != this.expectedText) {
                    description.appendText(" value: ");
                    description.appendText(this.expectedText);
                }

            }

            public boolean matchesSafely(View view) {
                if (null == this.expectedText) {
                    try {
                        this.expectedText = view.getResources().getString(resourceId);
                        this.resourceName = view.getResources().getResourceEntryName(resourceId);
                    } catch (Resources.NotFoundException ignored) {
                    }
                }

                if (view.getContentDescription() == null) {
                    return false;
                }

                return null != this.expectedText && this.expectedText.equals(view.getContentDescription().toString());
            }
        };
    }

    public static Matcher<View> invertMatcher(Matcher<View> originalMatcher, boolean invert) {
        if (!invert) {
            return originalMatcher;
        }
        return not(originalMatcher);
    }
}
