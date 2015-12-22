package com.lach.common.async;

import android.os.AsyncTask;

public class AsyncTaskRunner {

    public void execute(AsyncTask asyncTask, Object... params) {
        asyncTask.execute(params);
    }

}
