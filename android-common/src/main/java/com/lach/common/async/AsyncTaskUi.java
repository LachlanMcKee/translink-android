package com.lach.common.async;

public interface AsyncTaskUi {

    void onTaskFinished(int taskId, AsyncResult result);

    void onTaskCancelled(int taskId);

    boolean onTaskError(int taskId, int errorId);

}
