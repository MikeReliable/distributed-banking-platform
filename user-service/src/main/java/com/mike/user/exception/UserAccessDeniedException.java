package com.mike.user.exception;

import com.mike.user.common.ApiException;

public class UserAccessDeniedException extends ApiException {

    public UserAccessDeniedException() {
        super("FORBIDDEN", 403, "Access denied");
    }
}
