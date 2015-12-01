package com.lach.translink.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lach.common.util.NetworkUtil;
import com.lach.common.ui.view.Debouncer;
import com.lach.translink.ui.ViewModelFragment;
import com.lach.translink.ui.gocard.GoCardInfoActivity;
import com.lach.translink.activities.R;
import com.lach.translink.ui.search.dialog.SaveJourneyDialog;
import com.lach.translink.ui.search.dialog.WebsiteLinksDialog;
import com.lach.translink.ui.settings.SettingsActivity;

public class SearchFragment extends ViewModelFragment<SearchViewModel> {

    public static SearchFragment newInstance(Bundle bdl) {
        SearchFragment f = new SearchFragment();
        f.setArguments(bdl);
        return f;
    }

    private final Debouncer debouncer = new Debouncer();

    @Override
    public SearchViewModel createViewModel() {
        return new SearchViewModel(this, debouncer);
    }

    @Override
    public int getLayoutId() {
        return R.layout.f_search;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tell the application that the fragment is allowed an options menu.
        this.setHasOptionsMenu(true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.m_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!debouncer.isValidClick()) {
            return true;
        }

        SearchViewModel viewModel = getViewModel();
        FragmentActivity activity = getActivity();

        switch (item.getItemId()) {
            case R.id.menu_save_fav:
                SaveJourneyDialog saveJourneyDialog = SaveJourneyDialog.newInstance(viewModel.createJourneyCriteria());
                saveJourneyDialog.show(activity.getSupportFragmentManager(), "dialog");
                return true;

            case R.id.menu_load_fav:
                viewModel.loadJourney();
                return true;

            case R.id.menu_history:
                viewModel.showHistory();
                return true;

            case R.id.menu_settings:
                Intent settingsIntent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(settingsIntent);
                return true;

            case R.id.menu_time_now:
                viewModel.setCurrentTime();
                return true;

            case R.id.menu_go_card:
                if (!NetworkUtil.isOnline(activity)) {
                    return true;
                }

                Intent goCardInfoIntent = new Intent(activity, GoCardInfoActivity.class);
                activity.startActivity(goCardInfoIntent);
                return true;

            case R.id.menu_links:
                WebsiteLinksDialog webLinksDialog = WebsiteLinksDialog.newInstance();
                webLinksDialog.show(activity.getSupportFragmentManager(), "dialog");
                return true;

        }
        return false;
    }

}
