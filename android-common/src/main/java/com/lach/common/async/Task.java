package com.lach.common.async;

public interface Task<T> {

    AsyncResult<T> execute(Object... params);

}
