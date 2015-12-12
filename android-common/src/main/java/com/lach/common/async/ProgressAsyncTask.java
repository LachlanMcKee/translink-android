package com.lach.common.async;

import android.os.AsyncTask;

import com.lach.common.log.Log;

public class ProgressAsyncTask extends AsyncTask<Object, Object, AsyncResult> {
    private static final String TAG = "ProgressAsyncTask";

    private final Task task;

    private AsyncTaskFragment.TaskFragment mTaskFragment;

    public ProgressAsyncTask(Task task) {
        this.task = task;
    }

    void setTaskFragment(AsyncTaskFragment.TaskFragment fragment) {
        mTaskFragment = fragment;
    }

    @Override
    protected final AsyncResult doInBackground(Object... params) {
        return task.execute(params);
    }

    @Override
    protected void onPostExecute(AsyncResult result) {
        if (isCancelled()) {
            return;
        }

        // If the fragment is gone, this task was likely not cancelled.
        if (mTaskFragment != null) {
            mTaskFragment.taskFinished(result);
        } else {
            Log.warn(TAG, "onPostExecute. Fragment was not found");
        }
    }

}
