package com.lach.common.async;

public class UnitTestingTaskBuilder extends TaskBuilder {
    private PostExecuteListener listener;

    public UnitTestingTaskBuilder(int taskId, Task task, PostExecuteListener listener) {
        super(taskId, task);
        this.listener = listener;
    }

    @Override
    public void start() {
        AsyncResult result = task.execute(parameters);
        listener.onTaskExecuted(taskId, result);
    }

    public interface PostExecuteListener {
        void onTaskExecuted(int id, AsyncResult result);
    }
}