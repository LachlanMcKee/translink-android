package com.lach.translink.ui.impl.result;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.ui.dialog.AppCompatProgressDialog;
import com.lach.common.log.Log;
import com.lach.common.util.DialogUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.history.JourneyCriteriaHistory;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.tasks.result.TaskJourneySearch;
import com.lach.translink.webview.DaggerWebViewComponent;
import com.lach.translink.webview.WebViewComponent;
import com.lachlanm.xwalkfallback.WebViewFacade;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

public class JourneyResultFragment extends AsyncTaskFragment {
    private static final String TAG = "JourneyResultFragment";

    public static JourneyResultFragment newInstance(JourneyCriteria journeyCriteria, Date date) {
        JourneyResultFragment fragment = new JourneyResultFragment();

        Bundle b = new Bundle(2);
        b.putParcelable(BUNDLE_JOURNEY_SEARCH, journeyCriteria);
        b.putSerializable(BUNDLE_JOURNEY_DATE, date);
        fragment.setArguments(b);
        return fragment;
    }

    // Task ids
    private static final int TASK_SEARCH_FOR_JOURNEY = 1;

    // Bundle properties
    private static final String BUNDLE_JOURNEY_SEARCH = "journey_search";
    private static final String BUNDLE_JOURNEY_DATE = "journey_date";

    // Persisted properties
    private static final String CONTENT_LOADED_KEY = "content_loaded";

    private static final int WEB_READY_PERCENTAGE = 65;

    private View rootView;
    private ViewGroup webViewContainer;

    @Inject
    JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    @Inject
    WebViewFacade webViewFacade;

    @Inject
    Provider<TaskJourneySearch> taskJourneySearchProvider; // This must be lazy so we can initialize a WebView prior.

    private AppCompatProgressDialog progressDialog;

    private boolean initialContentLoaded = false;
    private JourneyResultListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (JourneyResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onViewSelected");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_journey_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.debug(TAG, "onViewCreated");

        FragmentActivity activity = getActivity();

        if (savedInstanceState == null) {
            showLoadingDialog(false);
        }

        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        WebViewComponent webViewComponent = DaggerWebViewComponent.builder()
                .coreModule(application.getCoreModule())
                .build();
        webViewComponent.inject(this);

        rootView = view.findViewById(R.id.main_content);
        webViewContainer = ((ViewGroup) view.findViewById(R.id.web_view_placeholder));

        View webView = webViewFacade.getView(activity);
        webViewContainer.addView(webView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Ensure any previous searches don't affect this new webView.
        webViewFacade.clearCache(true);

        // Slight appearance adjustments
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webViewFacade.setProgressListener(new WebViewFacade.ProgressListener() {
            @Override
            public void onProgressChanged(WebViewFacade facade, int progressInPercent) {
                if (!initialContentLoaded) {
                    Log.debug(TAG, "onProgressChanged. progressInPercent: " + progressInPercent);

                    if (progressInPercent >= WEB_READY_PERCENTAGE) {
                        webViewContainer.setVisibility(View.VISIBLE);
                        initialContentLoaded = true;
                        dismissLoadingDialog();

                        rootView.setBackgroundColor(Color.WHITE);

                        listener.onContentLoaded();
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            Log.debug(TAG, "startTask");

            // Load the journey criteria.
            JourneyCriteria journeyCriteria = getArguments().getParcelable(BUNDLE_JOURNEY_SEARCH);
            Date date = (Date) getArguments().getSerializable(BUNDLE_JOURNEY_DATE);

            createTask(TASK_SEARCH_FOR_JOURNEY, taskJourneySearchProvider.get())
                    .parameters(journeyCriteria, date, webViewFacade.getUserAgent())
                    .start(this);
        } else {
            boolean hasContentLoaded = savedInstanceState.getBoolean(CONTENT_LOADED_KEY);
            Log.debug(TAG, "restoreState. hasContentLoaded: " + hasContentLoaded);

            if (hasContentLoaded) {
                rootView.setBackgroundColor(Color.WHITE);
            }

            showLoadingDialog(hasContentLoaded);
            webViewFacade.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.debug(TAG, "onResume");

        if (webViewFacade != null) {
            webViewFacade.resume();
        }
    }

    @Override
    public void onPause() {
        Log.debug(TAG, "onPause");
        if (webViewFacade != null) {
            webViewFacade.pause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.debug(TAG, "onStop");
        dismissLoadingDialog();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.debug(TAG, "onDestroy");
        if (webViewFacade != null) {
            webViewFacade.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.debug(TAG, "onSaveInstanceState");

        webViewFacade.saveState(outState);
        outState.putBoolean(CONTENT_LOADED_KEY, initialContentLoaded);

        super.onSaveInstanceState(outState);
    }

    public boolean handleBackPressed() {
        Log.debug(TAG, "handleBackPressed");
        if (webViewFacade.canGoBack()) {
            webViewFacade.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onTaskFinished(int taskId, AsyncResult result) {
        Log.debug(TAG, "onTaskFinished");
        TaskJourneySearch.JourneyResponse item = (TaskJourneySearch.JourneyResponse) result.getItem();
        if (item == null) {
            Log.debug(TAG, "onTaskFinished. Item is null");
            return;
        }
        Log.debug("TAG", item.url);

        addJourneyHistory(item.criteria);

        Log.debug(TAG, "onTaskFinished. Loading WebView");
        webViewFacade.load(item.url, item.content);
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
            case TaskJourneySearch.ERROR_INVALID_CONTENT:
                title = "Invalid response";
                message = "Unfortunately this criteria has created an invalid response.\n" +
                        "This issue will be investigated.";
                break;

            case TaskJourneySearch.ERROR_INVALID_DATE:
                title = "Invalid date supplied";
                message = "Please ensure that your device has the correct time and try again.";
                break;

            case TaskJourneySearch.ERROR_INVALID_LOCATION:
                title = "Supplied locations incorrect";
                message = "Unfortunately either your starting point or destination isn't quite correct.\n\n" +
                        "This will be fixed in the next release.\n\n" +
                        "For now, please reconfirm both locations by using the magnifying glass button above.";
                break;

            case TaskJourneySearch.ERROR_INVALID_RESULTS:
                title = "No journey found";
                message = "Unfortunately there were no journeys which met your criteria.\n\nPlease try changing your criteria.";
                break;
        }

        if (title != null) {
            AlertDialog.Builder alertDialog = DialogUtil.createAlertDialog(activity, message, title, false);
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
        Log.debug(TAG, "onTaskCancelled");
    }

    @Override
    public void onTaskGenericErrorDismissed(int taskId) {
        Log.debug(TAG, "onTaskGenericErrorDismissed");
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void showLoadingDialog(boolean restoreContent) {
        Log.debug(TAG, "showLoadingDialog");
        dismissLoadingDialog();

        String messageType = restoreContent ? "Restoring" : "Loading";

        progressDialog = new AppCompatProgressDialog(getActivity());
        progressDialog.setMessage(messageType + " journey");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
            }
        });
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        Log.debug(TAG, "dismissLoadingDialog");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void addJourneyHistory(JourneyCriteria criteria) {
        Log.debug(TAG, "addJourneyHistory");

        JourneyCriteriaHistory history = journeyCriteriaHistoryDao.createModel();
        history.setDateCreated(new Date());
        history.setJourneyCriteria(criteria);

        journeyCriteriaHistoryDao.insertRows(false, history);
    }

    public interface JourneyResultListener {
        void onContentLoaded();
    }

}
