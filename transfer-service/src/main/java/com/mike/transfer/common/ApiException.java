package com.mike.transfer.common;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final String type;
    private final int status;

    protected ApiException(String type, int status, String message) {
        super(message);
        this.type = type;
        this.status = status;
    }
}
