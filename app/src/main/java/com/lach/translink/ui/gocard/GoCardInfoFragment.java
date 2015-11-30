package com.lach.translink.ui.gocard;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.log.Log;
import com.lach.common.util.DialogUtil;
import com.lach.common.util.NetworkUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.gocard.GoCardDetails;
import com.lach.translink.data.gocard.GoCardTransaction;
import com.lach.translink.tasks.gocard.TaskGoCardDetails;
import com.lach.translink.tasks.gocard.TaskGoCardHistory;
import com.lach.translink.network.GoCardNetworkComponent;
import com.lach.translink.network.GoCardNumberValid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GoCardInfoFragment extends AsyncTaskFragment {
    private static final String TAG = GoCardInfoFragment.class.getSimpleName();

    @InjectView(R.id.gocard_balance_text)
    TextView goCardBalanceText;

    @InjectView(R.id.gocard_type_text)
    TextView goCardTypeText;

    @InjectView(R.id.gocard_issue_text)
    TextView goCardIssueText;

    @InjectView(R.id.gocard_expire_text)
    TextView goCardExpireText;

    @InjectView(R.id.gocard_balance_as_of_text)
    TextView goCardBalanceAsOfText;

    @InjectView(R.id.gocard_history_description)
    TextView goCardHistoryDescriptionText;

    @InjectView(R.id.gocard_history_list)
    ListView historyList;

    private GoCardDetails goCardDetails;
    private ArrayList<GoCardTransaction> transactions;

    @Inject
    @GoCardNumberValid
    boolean cardNumberValid;

    @Inject
    @GoCardNumberValid
    boolean cardPasswordValid;

    private Date currentEndDate;

    private static final int TASK_GOCARD_DETAILS = 1;
    private static final int TASK_GOCARD_HISTORY = 2;

    private static final String GRAPH_FRAGMENT_TAG = "graph_storage";

    @Inject
    Provider<TaskGoCardDetails> goCardDetailsTaskProvider;

    @Inject
    Provider<TaskGoCardHistory> goCardHistoryTaskProvider;

    public static GoCardInfoFragment newInstance() {
        GoCardInfoFragment f = new GoCardInfoFragment();
        Bundle bdl = new Bundle(0);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("goCardDetails", goCardDetails);
        outState.putParcelableArrayList("historyValues", transactions);
        outState.putLong("endDateMillis", currentEndDate.getTime());
        super.onSaveInstanceState(outState);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_gocard_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View footerView = View.inflate(getActivity(), R.layout.f_gocard_info_footer, null);
        Button loadHistoryButton = ButterKnife.findById(footerView, R.id.load_history);
        loadHistoryButton.setOnClickListener(loadHistoryButtonListener);
        historyList.addFooterView(footerView);

        if (savedInstanceState != null) {
            currentEndDate = new Date(savedInstanceState.getLong("endDateMillis", -1));
            updateHistoryDescription();

            restoreGoCardInfo(savedInstanceState);
        } else {
            currentEndDate = new Date();
            transactions = new ArrayList<>();
            loadHistory();
        }

        FragmentManager fm = getFragmentManager();
        GoCardGraphFragment graphFragment = (GoCardGraphFragment) fm.findFragmentByTag(GRAPH_FRAGMENT_TAG);
        if (graphFragment == null) {
            graphFragment = new GoCardGraphFragment();
            graphFragment.setGraphCreatedListener(new GoCardGraphFragment.GraphCreatedListener() {
                @Override
                public void onGraphCreated(GoCardGraphFragment graphFragment) {
                    graphFragment.inject(GoCardInfoFragment.this);
                    checkFirstLoad();
                }
            });

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.graph_fragment_container, graphFragment, GRAPH_FRAGMENT_TAG);
            ft.commit();
        } else {
            graphFragment.inject(this);
            checkFirstLoad();
        }
    }

    private void checkFirstLoad() {
        // If this is the first time loading, we request the data.
        if (!isTaskRunning() && goCardDetails == null) {
            obtainGoCardData();
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
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

    private final OnClickListener loadHistoryButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TaskGoCardHistory taskGoCardHistory = goCardHistoryTaskProvider.get();
            taskGoCardHistory.setEndDate(currentEndDate);
            startTask(TASK_GOCARD_HISTORY, taskGoCardHistory, "Loading Go-Card history");
        }
    };

    private void restoreGoCardInfo(Bundle savedInstanceState) {
        goCardDetails = savedInstanceState.getParcelable("goCardDetails");
        transactions = savedInstanceState.getParcelableArrayList("historyValues");

        loadGoCardDetails(goCardDetails);
        loadHistory();
    }

    private void loadGoCardDetails(GoCardDetails goCardDetails) {
        if (goCardDetails != null) {
            goCardBalanceText.setText(goCardDetails.balance);
            goCardBalanceAsOfText.setText("As of " + goCardDetails.balanceTime);
            goCardTypeText.setText(goCardDetails.passengerType);
            goCardIssueText.setText(goCardDetails.issueDate);
            goCardExpireText.setText(goCardDetails.expiryDate);

            this.goCardDetails = goCardDetails;
        }
    }

    private void loadHistory() {
        GoCardTransactionArrayAdapter adaptor = new GoCardTransactionArrayAdapter(getActivity(), transactions);
        historyList.setAdapter(adaptor);

        historyList.post(new Runnable() {
            public void run() {
                if (historyList != null) {
                    historyList.setSelection(historyList.getCount() - 1);
                }
            }
        });
    }

    private void obtainGoCardData() {
        if (!cardNumberValid || !cardPasswordValid) {
            AlertDialog showDialog = DialogUtil.showAlertDialog(getActivity(),
                    "You have not set your Go-Card details, please update these within the settings screen.", "Go-Card details missing");
            showDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    getActivity().finish();
                }
            });
        } else {
            if (NetworkUtil.isOnline(getActivity())) {
                startTask(TASK_GOCARD_DETAILS, goCardDetailsTaskProvider.get(), "Loading Go-Card information");

            } else {
                getActivity().finish();
            }
        }
    }

    private void updateHistoryDescription() {
        // Update the history description.
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        goCardHistoryDescriptionText.setText("(until " + sdf.format(currentEndDate) + ")");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        if (taskId == TASK_GOCARD_DETAILS) {
            loadGoCardDetails((GoCardDetails) result.getItem());

        } else if (taskId == TASK_GOCARD_HISTORY) {
            this.transactions.addAll((ArrayList<GoCardTransaction>) result.getItem());
            loadHistory();

            // Update the current date so the user can search further back.
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentEndDate);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MONTH, -TaskGoCardHistory.HISTORY_MONTHS);
            currentEndDate = cal.getTime();

            updateHistoryDescription();
        }
    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return false;
        }

        String title = null;
        String message = null;

        switch (errorId) {
            case TaskGoCardDetails.ERROR_CONNECTION_TROUBLE:
                title = "Go-card error";
                message = "There was a problem trying to connect to the translink go-card website.\n\n" +
                        "The website may currently be down. Please ensure you have access to the Internet.\n\n" +
                        "Verify you can access the following:\nhttps://gocard.translink.com.au";
                break;

            case TaskGoCardDetails.ERROR_BAD_PARSING:
                title = "Go-card error";
                message = "There was an error trying to read the website data.\nThis issue will be investigated.";
                break;

            case TaskGoCardDetails.ERROR_INVALID_CREDENTIALS:
                title = "Go-card error";
                message = "Unable to log in. Please check the following:\n\n"
                        + "- Is your username and password is correct?\n"
                        + "- Has your password expired?\n\n"
                        + "https://gocard.translink.com.au/webtix/";
                break;
        }

        if (title != null) {
            AlertDialog.Builder alertDialog = DialogUtil.createAlertDialog(activity, message, title, true);
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onTaskGenericErrorDismissed(-1);
                }
            });
            alertDialog.show();
            return true;
        }

        return super.onTaskError(taskId, errorId);
    }

    @Override
    public void onTaskCancelled(int taskId) {

    }

    @Override
    public void onTaskGenericErrorDismissed(int taskId) {
        Log.debug(TAG, "onTaskGenericErrorDismissed");
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    public static class GoCardGraphFragment extends Fragment {
        private GoCardNetworkComponent objectGraph;
        private GraphCreatedListener listener;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setRetainInstance(true);

            TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
            objectGraph = application.createGoCardNetworkComponent();

            objectGraph.injectFragment(this);

            if (listener != null) {
                listener.onGraphCreated(this);
            }
        }

        public void inject(GoCardInfoFragment fragment) {
            if (objectGraph != null) {
                objectGraph.injectFragment(fragment);
            }
        }

        public void setGraphCreatedListener(GraphCreatedListener listener) {
            this.listener = listener;
        }

        interface GraphCreatedListener {
            void onGraphCreated(GoCardGraphFragment graphFragment);
        }
    }
}