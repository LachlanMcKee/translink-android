package com.lach.common.data;

import com.lach.common.network.UnexpectedResponseException;

import java.io.IOException;

public final class TaskGenericErrorType {
    public static final int NO_ERROR = -1;

    public static final int NOT_SPECIFIED = 1000;
    public static final int IO_FAILURE = 1001;
    public static final int INVALID_NETWORK_RESPONSE = 1002;

    public static int findGenericErrorTypeByException(Class<? extends Exception> exception) {
        if (exception.equals(IOException.class)) {
            return TaskGenericErrorType.IO_FAILURE;

        } else if (exception.equals(UnexpectedResponseException.class)) {
            return TaskGenericErrorType.INVALID_NETWORK_RESPONSE;
        }
        return TaskGenericErrorType.NOT_SPECIFIED;
    }
}
