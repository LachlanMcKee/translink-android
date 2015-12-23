package com.lach.common.network;

public class UnexpectedResponseException extends Exception {
    public UnexpectedResponseException(String detailMessage) {
        super(detailMessage);
    }
}
