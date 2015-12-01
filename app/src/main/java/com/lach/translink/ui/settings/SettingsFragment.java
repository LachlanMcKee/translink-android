package com.lach.translink.ui.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lach.common.data.common.CommonPreference;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.ui.view.PreferenceListFragment;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.ui.gocard.GoCardDetailsDialog;

import javax.inject.Inject;

public class SettingsFragment extends PreferenceListFragment {

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();

        Bundle b = new Bundle();
        populateBundle(b, R.xml.menu);
        fragment.setArguments(b);

        return fragment;
    }

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    private String currentThemeName;
    private boolean isRecreating;

    private Preference getPreference(int resID) {
        return getPreferenceScreen().findPreference(getString(resID));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Add the action bar back button.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        Preference locationsPreference = getPreference(R.string.Locations_Preference_Menu);
        Intent locationList = new Intent(getActivity(), FavouriteLocationsActivity.class);
        locationsPreference.setIntent(locationList);

        Preference journeysPreference = getPreference(R.string.Journeys_Preference_Menu);
        Intent favouritesList = new Intent(getActivity(), FavouriteJourneysActivity.class);
        journeysPreference.setIntent(favouritesList);

        Preference clearHistoryPreference = getPreference(R.string.Clear_History_Menu);
        clearHistoryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearHistory();
                return true;
            }
        });

        Preference gocardPreference = getPreference(R.string.Go_Card_Preference_Menu);
        gocardPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                GoCardDetailsDialog newFragment = GoCardDetailsDialog.newInstance();
                newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                return true;
            }
        });

        currentThemeName = CommonPreference.THEME.get(preferencesProvider.getPreferences());

        Preference themePreference = getPreference(R.string.theme_pref_key);
        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                if (!currentThemeName.equals(newVal)) {
                    recreate();
                }
                return true;
            }

        });

    }

    private synchronized void recreate() {
        if (isRecreating) {
            // Ensure recreating is not called multiple times.
            return;
        }
        isRecreating = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().recreate();
        } else {
            getActivity().finish();
            startActivity(getActivity().getIntent());
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void clearHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Clear History").setCancelable(true);
        builder.setMessage("Are you sure you want to clear your journey history?").setCancelable(true);
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                journeyCriteriaHistoryDao.deleteAllRows();

                Toast.makeText(getActivity(), "History cleared", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
