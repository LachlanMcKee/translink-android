package com.lach.common.async;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;

import com.lach.common.ui.BaseActivity;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.ui.dialog.AppCompatProgressDialog;
import com.lach.common.ui.ButterFragment;
import com.lach.common.log.Log;
import com.lach.common.util.DialogUtil;

import java.lang.ref.WeakReference;

public abstract class AsyncTaskFragment extends ButterFragment implements AsyncTaskUi {
    private static final String TAG = "AsyncTaskFragment";

    // Code to identify the fragment that is calling onActivityResult(). We don't really need
    // this since we only have one fragment to deal with.
    private static final int TASK_FRAGMENT = 0;

    // Tag so we can find the task fragment again, in another instance of this fragment after rotation.
    private static final String TASK_FRAGMENT_TAG = "task";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check to see if we have retained the worker fragment.
        Fragment taskFragment = getFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);

        if (taskFragment != null) {
            Log.debug(TAG, "onCreate. Reassigning taskFragment");
            // Update the target fragment so it goes to this fragment instead of the old one.
            // This will also allow the GC to reclaim the old MainFragment, which the DialogTaskFragment
            // keeps a reference to. Note that I looked in the code and setTargetFragment() doesn't
            // use weak references. To be sure you aren't leaking, you may wish to make your own
            // setTargetFragment() which does.
            taskFragment.setTargetFragment(this, TASK_FRAGMENT);
        }
    }

    @Override
    public void onDestroy() {
        BaseActivity activity = (BaseActivity) getActivity();
        if (!activity.isChangingConfigurations()) {
            cancelCurrentTask(false);
        }
        super.onDestroy();
    }

    @Override
    public boolean onTaskError(int taskId, int errorId) {
        return false;
    }

    @SuppressWarnings("UnusedParameters")
    protected void onTaskGenericErrorDismissed(int taskId) {
        // Do nothing.
    }

    @SuppressLint("InflateParams")
    private void taskGenericError(final int taskId, int errorId) {
        Context context = getActivity();

        if (context == null) {
            return;
        }

        String title = null;
        String message;
        switch (errorId) {
            case TaskGenericErrorType.NETWORK_FAILURE:
                title = "Connectivity problem";
                message = "A network error has occurred. Please check your Internet connection.";
                break;

            case TaskGenericErrorType.INVALID_NETWORK_RESPONSE:
                title = "Communication problem";
                message = "There was a problem communicating with the server. Please try again later.";
                break;

            case TaskGenericErrorType.NOT_SPECIFIED:
            default:
                message = "An unknown error has occurred. This will be investigated.";
                break;
        }

        AlertDialog.Builder builder = DialogUtil.createAlertDialog(context, message, title, false)
                .setPositiveButton("Ok", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        onTaskGenericErrorDismissed(taskId);
                    }
                });

        builder.show();
    }

    public static class TaskBuilder {
        final int taskId;
        final Task task;
        Object[] parameters;
        String message;

        public TaskBuilder(int taskId, Task task) {
            this.taskId = taskId;
            this.task = task;
        }

        public TaskBuilder parameters(Object... parameters) {
            this.parameters = parameters;
            return this;
        }

        public TaskBuilder message(String message) {
            this.message = message;
            return this;
        }

        public void start(AsyncTaskFragment fragment) {
            fragment.startTaskInternal(taskId, task, message, parameters);
        }
    }

    public TaskBuilder createTask(int taskId, Task task) {
        return new TaskBuilder(taskId, task);
    }

    public void startTask(int taskSearchForJourney, Task task) {
        createTask(taskSearchForJourney, task).start(this);
    }

    public void startTask(int taskSearchForJourney, Task task, String message) {
        createTask(taskSearchForJourney, task).message(message).start(this);
    }

    private void startTaskInternal(int taskId, Task task, String message, Object[] parameters) {
        TaskFragment taskFragment;

        if (message == null) {
            taskFragment = new HiddenTaskFragment();
        } else {
            taskFragment = new DialogTaskFragment();
        }

        taskFragment.setParameters(parameters);
        if (message != null) {
            taskFragment.setMessage(message);
        }

        // And create a task for it to monitor. In this implementation the taskFragment
        // executes the task, but you could change it so that it is started here.
        taskFragment.setTask(taskId, task);

        // And tell it to call onActivityResult() on this fragment.
        taskFragment.setTargetFragment(this, TASK_FRAGMENT);

        // Show the fragment.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (taskFragment instanceof DialogFragment) {
            ((DialogFragment) taskFragment).show(ft, TASK_FRAGMENT_TAG);
        } else {
            ft.add((Fragment) taskFragment, TASK_FRAGMENT_TAG);
            ft.commit();
        }
    }

    public interface TaskFragment {
        void setTask(int taskId, Task task);

        void taskFinished(AsyncResult result);

        void cancelTask(boolean notify);

        boolean isTaskActive();

        Context getContext();

        void setMessage(String message);

        void setParameters(Object[] parameters);

        void setTargetFragment(Fragment asyncTaskFragment, int taskFragment);
    }

    public boolean isTaskRunning() {
        Fragment taskFragment = getFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
        return taskFragment != null && ((TaskFragment) taskFragment).isTaskActive();
    }

    public void cancelCurrentTask(boolean notify) {
        Fragment taskFragment = getFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
        if (taskFragment != null) {
            Log.debug(TAG, "cancelCurrentTask. Notify: " + notify);
            ((TaskFragment) taskFragment).cancelTask(notify);
        }
    }

    private static abstract class TaskFragmentDelegate<T extends Fragment & TaskFragment> {
        private static final String TAG = "TaskFragment";

        // The task we are running.
        WeakReference<ProgressAsyncTask> mTaskRef;
        private int mTaskId = -1;
        private Object[] parameters;

        public void setParameters(Object[] parameters) {
            this.parameters = parameters;
        }

        public void setTask(int taskId, Task task) {
            Log.debug(TAG, "setTask. taskId: " + taskId);
            ProgressAsyncTask asyncTask = new ProgressAsyncTask(task);

            mTaskRef = new WeakReference<>(asyncTask);
            mTaskId = taskId;

            // Tell the AsyncTask to call updateProgress() and taskFinished() on this fragment.
            asyncTask.setTaskFragment(getTaskFragment());
        }

        public void executeTask() {
            Log.debug(TAG, "executeTask");
            if (mTaskRef == null) {
                return;
            }

            ProgressAsyncTask task = mTaskRef.get();
            Log.debug(TAG, "executeTask. task exists: " + (task != null));

            if (task != null) {
                //noinspection unchecked
                task.execute(parameters);
            }
        }

        public void cancelTask(boolean notify) {
            Log.debug(TAG, "cancelTask");
            if (mTaskRef == null) {
                return;
            }

            ProgressAsyncTask task = mTaskRef.get();
            Log.debug(TAG, "cancelTask. Task exists: " + (task != null));

            if (task != null) {
                task.cancel(true);
            }

            mTaskRef.clear();
            callDestroy();

            if (notify) {
                T taskFragment = getTaskFragment();
                AsyncTaskFragment asyncTaskFragment = (AsyncTaskFragment) taskFragment.getTargetFragment();
                if (asyncTaskFragment != null) {
                    asyncTaskFragment.onTaskCancelled(mTaskId);
                }
            }
        }

        public boolean isTaskActive() {
            return mTaskRef != null && mTaskRef.get() != null;
        }

        // This is also called by the AsyncTask.
        public void taskFinished(AsyncResult result) {
            Log.debug(TAG, "taskFinished");
            callDestroy();

            // If we aren't resumed, setting the task to null will allow us to dismiss ourselves in
            // onResume().
            if (mTaskRef != null) {
                mTaskRef.clear();
            }

            // Tell the fragment that we are done.
            T taskFragment = getTaskFragment();
            AsyncTaskFragment asyncTaskFragment = (AsyncTaskFragment) taskFragment.getTargetFragment();

            Log.debug(TAG, "taskFinished. asyncTaskFragment: " + asyncTaskFragment);
            if (asyncTaskFragment != null) {
                boolean hasError = result.hasError();
                int errorId = result.getErrorId();
                Log.debug(TAG, "taskFinished. hasError: " + hasError + ", errorId: " + errorId);

                if (!hasError) {
                    asyncTaskFragment.onTaskFinished(mTaskId, result);
                } else {
                    if (!asyncTaskFragment.onTaskError(mTaskId, errorId)) {
                        asyncTaskFragment.taskGenericError(mTaskId, errorId);
                    }
                }
            }
        }

        public void onResume() {
            boolean taskExists = (mTaskRef != null && mTaskRef.get() != null);
            Log.debug(TAG, "onResume. taskExists: " + taskExists);

            // This is a little hacky, but we will see if the task has finished while we weren't
            // in this activity, and then we can dismiss ourselves.
            if (!taskExists) {
                callDestroy();
            }
        }

        private void callDestroy() {
            T taskFragment = getTaskFragment();

            // Make sure we check if it is resumed because we will crash if trying to dismiss the dialog
            // after the user has switched to another app.
            boolean isResumed = taskFragment.isResumed();
            Log.debug(TAG, "callDestroy. isResumed: " + isResumed);
            if (isResumed) {
                destroy();
            }
        }

        public abstract T getTaskFragment();

        public abstract void destroy();
    }

    public static class HiddenTaskFragment extends Fragment implements TaskFragment {
        private final TaskFragmentDelegate delegate = new TaskFragmentDelegate<HiddenTaskFragment>() {
            @Override
            public HiddenTaskFragment getTaskFragment() {
                return HiddenTaskFragment.this;
            }

            @Override
            public void destroy() {
                // Remove self.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(HiddenTaskFragment.this);
                ft.commit();
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);

            delegate.executeTask();
        }

        @Override
        public void onResume() {
            super.onResume();
            delegate.onResume();
        }

        @Override
        public void setTask(int taskId, Task task) {
            delegate.setTask(taskId, task);
        }

        @Override
        public void taskFinished(AsyncResult result) {
            delegate.taskFinished(result);
        }

        @Override
        public void cancelTask(boolean notify) {
            delegate.cancelTask(notify);
        }

        @Override
        public boolean isTaskActive() {
            return delegate.isTaskActive();
        }

        @Override
        public Context getContext() {
            return getActivity();
        }

        @Override
        public void setMessage(String message) {
            // Do nothing.
        }

        @Override
        public void setParameters(Object[] parameters) {
            delegate.setParameters(parameters);
        }
    }

    public static class DialogTaskFragment extends DialogFragment implements TaskFragment {
        private String mMessage = "Running background process.";
        private final String mTitle = "Please wait...";
        private final TaskFragmentDelegate delegate = new TaskFragmentDelegate<DialogTaskFragment>() {
            @Override
            public DialogTaskFragment getTaskFragment() {
                return DialogTaskFragment.this;
            }

            @Override
            public void destroy() {
                DialogTaskFragment.this.dismiss();
            }
        };

        @Override
        public void setMessage(String message) {
            mMessage = message;
        }

        @Override
        public void setParameters(Object[] parameters) {
            delegate.setParameters(parameters);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);

            delegate.executeTask();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AppCompatProgressDialog dialog = new AppCompatProgressDialog(getActivity());
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mTitle != null) {
                dialog.setTitle(mTitle);
            }
            dialog.setCancelable(false);

            return dialog;
        }

        // This is to work around what is apparently a bug. If you don't have it
        // here the dialog will be dismissed on rotation, so tell it not to dismiss.
        @Override
        public void onDestroyView() {
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.setDismissMessage(null);
            }
            super.onDestroyView();
        }

        // Also when we are dismissed we need to cancel the task.
        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            delegate.cancelTask(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            delegate.onResume();
        }

        @Override
        public void setTask(int taskId, Task task) {
            delegate.setTask(taskId, task);
        }

        @Override
        public void taskFinished(AsyncResult result) {
            delegate.taskFinished(result);
        }

        @Override
        public void cancelTask(boolean notify) {
            delegate.cancelTask(notify);
        }

        @Override
        public boolean isTaskActive() {
            return delegate.isTaskActive();
        }

        @Override
        public Context getContext() {
            return getActivity();
        }
    }

}
