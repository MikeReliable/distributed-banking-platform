package com.mike.transfer.error;

import com.mike.transfer.common.ApiError;
import com.mike.transfer.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import static com.mike.transfer.error.ErrorType.INTERNAL_ERROR;
import static com.mike.transfer.error.ErrorType.VALIDATION_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return badRequest(
                VALIDATION_ERROR.name(),
                detail,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                VALIDATION_ERROR.name(),
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(ex.getStatus()).body(
                buildError(
                        ex.getType(),
                        ex.getMessage(),
                        ex.getStatus(),
                        ex.getMessage(),
                        request
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildError(
                        INTERNAL_ERROR.name(),
                        "Internal error",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected error",
                        request
                )
        );
    }

    private ResponseEntity<ApiError> badRequest(
            String type,
            String detail,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                buildError(
                        type,
                        "Validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        detail,
                        request
                )
        );
    }

    private ApiError buildError(
            String type,
            String title,
            int status,
            String detail,
            HttpServletRequest request
    ) {
        return new ApiError(
                type,
                title,
                status,
                detail,
                request.getRequestURI(),
                resolveRequestId(),
                Instant.now()
        );
    }

    private String resolveRequestId() {
        String requestId = MDC.get("requestId");
        return requestId != null ? requestId : "N/A";
    }
}

