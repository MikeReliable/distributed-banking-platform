package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;

public class TransferAccessDeniedException extends ApiException {

    public TransferAccessDeniedException() {
        super("FORBIDDEN", 403, "Access denied");
    }
}
