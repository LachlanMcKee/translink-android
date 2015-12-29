package com.lach.translink.ui.view;

import com.lach.common.async.Task;
import com.lach.common.async.TaskBuilder;

public interface TaskView extends BaseView {
    TaskBuilder createTask(int taskId, Task task);

    void cancelCurrentTask(boolean notify);

    boolean isTaskRunning();
}
