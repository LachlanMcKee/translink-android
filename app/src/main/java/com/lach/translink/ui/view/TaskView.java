package com.lach.translink.ui.view;

import com.lach.common.async.AsyncTaskFragment;
import com.lach.common.async.Task;

public interface TaskView extends BaseView {
    AsyncTaskFragment.TaskBuilder createTask(int taskSearchTranslink, Task task);

    void cancelCurrentTask(boolean notify);

    boolean isTaskRunning();
}
