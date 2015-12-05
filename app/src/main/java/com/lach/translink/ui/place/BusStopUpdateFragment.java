package com.lach.translink.ui.place;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.log.Log;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.tasks.place.TaskInsertBusStops;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.InjectView;
import butterknife.OnClick;

public class BusStopUpdateFragment extends AsyncTaskFragment {

    public static BusStopUpdateFragment newInstance() {
        return new BusStopUpdateFragment();
    }

    private static final String TAG = BusStopUpdateFragment.class.getSimpleName();

    private static final int TASK_INSTALL = 0;

    @Inject
    Provider<TaskInsertBusStops> insertBusStopsTaskProvider;

    @InjectView(R.id.update_bus_stops_text)
    TextView label;

    @InjectView(R.id.update_bus_stops_spinner)
    ProgressBar spinner;

    @InjectView(R.id.update_bus_stops_continue_button)
    Button continueButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        if (!isTaskRunning()) {
            Log.debug(TAG, "Starting install task.");
            startTask(TASK_INSTALL, insertBusStopsTaskProvider.get());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_bus_stop_install, container, false);
    }

    @Override
    protected void onTaskFinished(int taskId, AsyncResult result) {
        label.setText(R.string.update_bus_finished_label);
        spinner.setVisibility(View.GONE);
        continueButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onTaskCancelled(int taskId) {

    }

    @OnClick(R.id.update_bus_stops_continue_button)
    void close() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
