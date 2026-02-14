package com.mike.user.common;

import com.mike.user.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.time.Instant;

public class ApiErrorBuilder {

    public static ApiError build(ErrorType errorType, String detail, HttpServletRequest request) {
        return new ApiError(
                errorType.name(),
                errorType.getTitle(),
                errorType.getStatus().value(),
                detail,
                request.getRequestURI(),
                resolveRequestId(),
                Instant.now()
        );
    }

    public static String resolveRequestId() {
        String requestId = MDC.get("requestId");
        return requestId != null ? requestId : "N/A";
    }
}