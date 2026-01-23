package com.mike.transfer.error;

import com.mike.transfer.common.ApiException;

public class TransferException extends ApiException {
    public TransferException(ErrorType type, String message) {
        super(type.name(), 400, message);
    }
}
