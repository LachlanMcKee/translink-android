package com.lach.common.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.lach.common.R;
import com.lach.common.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PreferenceListFragment extends ListFragment {
    private static final String TAG = "PreferenceListFragment";
    private static final String BUNDLE_XML_ID = "xml_id";

    /**
     * The starting request code given out to preference framework.
     */
    private static final int FIRST_REQUEST_CODE = 100;
    
    private PreferenceManager mPreferenceManager;
    private ListView listView;

    @SuppressWarnings("SameParameterValue")
    protected static void populateBundle(Bundle b, int xmlId) {
        b.putInt(BUNDLE_XML_ID, xmlId);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        mPreferenceManager = onCreatePreferenceManager();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = getListView();
        listView.setSelector(R.drawable.ripple);

        int xmlId = getArguments().getInt(BUNDLE_XML_ID);
        addPreferencesFromResource(xmlId);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception ex) {
            Log.error(TAG, "onStop", ex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception ex) {
            Log.error(TAG, "onDestroy", ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
            m.setAccessible(true);
            m.invoke(mPreferenceManager, requestCode, resultCode, data);
        } catch (Exception ex) {
            Log.error(TAG, "onActivityResult", ex);
        }
    }

    private void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null && listView != null) {
            preferenceScreen.bind(listView);
            setListShown(true);
        }
    }

    /**
     * Creates the {@link android.preference.PreferenceManager}.
     *
     * @return The {@link android.preference.PreferenceManager} used by this activity.
     */
    private PreferenceManager onCreatePreferenceManager() {
        try {
            Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
            c.setAccessible(true);
            return c.newInstance(this.getActivity(), FIRST_REQUEST_CODE);
        } catch (Exception ex) {
            Log.error(TAG, "onCreatePreferenceManager", ex);
            return null;
        }
    }

    /**
     * Returns the {@link android.preference.PreferenceManager} used by this activity.
     *
     * @return The {@link android.preference.PreferenceManager}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    /**
     * Gets the root of the preference hierarchy that this activity is showing.
     *
     * @return The {@link android.preference.PreferenceScreen} that is the root of the preference
     * hierarchy.
     */
    protected PreferenceScreen getPreferenceScreen() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            m.setAccessible(true);
            return (PreferenceScreen) m.invoke(mPreferenceManager);
        } catch (Exception ex) {
            Log.error(TAG, "getPreferenceScreen", ex);
            return null;
        }
    }

    /**
     * Sets the root of the preference hierarchy that this activity is showing.
     *
     * @param preferenceScreen The root {@link android.preference.PreferenceScreen} of the preference hierarchy.
     */
    private void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            m.setAccessible(true);
            boolean result = (Boolean) m.invoke(mPreferenceManager, preferenceScreen);
            if (result && preferenceScreen != null) {
                bindPreferences();
            }
        } catch (Exception ex) {
            Log.error(TAG, "setPreferenceScreen", ex);
        }
    }

    /**
     * Adds preferences from activities that match the given {@link android.content.Intent}.
     *
     * @param intent The {@link android.content.Intent} to query activities.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void addPreferencesFromIntent(Intent intent) {
        throw new RuntimeException("too lazy to include this bs");
    }

    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     *
     * @param preferencesResId The XML resource ID to inflate.
     */
    private void addPreferencesFromResource(int preferencesResId) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen prefScreen = (PreferenceScreen) m.invoke(mPreferenceManager, getActivity(), preferencesResId, getPreferenceScreen());
            setPreferenceScreen(prefScreen);
        } catch (Exception ex) {
            Log.error(TAG, "addPreferencesFromResource", ex);
        }
    }

    /**
     * Finds a {@link android.preference.Preference} based on its key.
     *
     * @param key The key of the preference to retrieve.
     * @return The {@link android.preference.Preference} with the key, or null.
     * @see android.preference.PreferenceGroup#findPreference(CharSequence)
     */
    @SuppressWarnings("UnusedDeclaration")
    public Preference findPreference(CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }

}