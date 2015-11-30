package com.lach.common.http;

public class UnexpectedResponseException extends Exception {
    public UnexpectedResponseException(String detailMessage) {
        super(detailMessage);
    }
}
