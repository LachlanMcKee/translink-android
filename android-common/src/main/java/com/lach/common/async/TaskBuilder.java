package com.lach.common.async;

public abstract class TaskBuilder {
    final int taskId;
    final Task task;
    Object[] parameters;
    String message;
    boolean executeImmediately = false;

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

    public TaskBuilder executeImmediately(boolean executeImmediately) {
        this.executeImmediately = executeImmediately;
        return this;
    }

    public abstract void start();
}
