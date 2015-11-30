package com.lach.common.async;

import com.lach.common.data.TaskGenericErrorType;

public class AsyncResult<T> {

    private T item = null;
    private int errorId = TaskGenericErrorType.NO_ERROR;

    public T getItem() {
        return item;
    }

    public boolean hasError() {
        return errorId != TaskGenericErrorType.NO_ERROR;
    }

    public int getErrorId() {
        return errorId;
    }

    public AsyncResult(T item) {
        this.item = item;
    }

    public AsyncResult(int errorId) {
        this.errorId = errorId;
    }

}
