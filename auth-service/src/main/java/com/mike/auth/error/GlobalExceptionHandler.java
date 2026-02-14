package com.mike.auth.error;

import com.mike.auth.common.ApiError;
import com.mike.auth.common.ApiErrorBuilder;
import com.mike.auth.common.ApiException;
import com.mike.auth.exception.AuthForbiddenException;
import com.mike.auth.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        String detail = ex.getMessage();
        logError(ErrorType.INVALID_CREDENTIALS, detail, request, ex);
        return buildResponse(ErrorType.INVALID_CREDENTIALS, detail, request);
    }

    @ExceptionHandler(AuthForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(
            AuthForbiddenException ex,
            HttpServletRequest request
    ) {
        String detail = ex.getMessage();
        logError(ErrorType.FORBIDDEN, detail, request, ex);
        return buildResponse(ErrorType.FORBIDDEN, detail, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        logError(ErrorType.VALIDATION_ERROR, detail, request, ex);
        return buildResponse(ErrorType.VALIDATION_ERROR, detail, request);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        ErrorType errorType = Arrays.stream(ErrorType.values())
                .filter(et -> et.name().equals(ex.getType()))
                .findFirst()
                .orElse(ErrorType.INTERNAL_ERROR);

        String detail = ex.getMessage();
        logError(errorType, detail, request, ex);
        return buildResponse(errorType, detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        String detail = "Unexpected error";
        logError(ErrorType.INTERNAL_ERROR, detail, request, ex);
        return buildResponse(ErrorType.INTERNAL_ERROR, detail, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String detail = ex.getMessage();
        logError(ErrorType.VALIDATION_ERROR, detail, request, ex);
        return buildResponse(ErrorType.VALIDATION_ERROR, detail, request);
    }

    private ResponseEntity<ApiError> buildResponse(ErrorType errorType, String detail, HttpServletRequest request) {
        ApiError error = ApiErrorBuilder.build(errorType, detail, request);
        return ResponseEntity.status(errorType.getStatus()).body(error);
    }

    private void logError(ErrorType errorType, String detail, HttpServletRequest request, Exception ex) {
        String requestId = ApiErrorBuilder.resolveRequestId();
        String message = String.format(
                "[requestId=%s] %s %s | status=%d | type=%s | detail=%s",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                errorType.getStatus().value(),
                errorType.name(),
                detail
        );

        if (errorType.getStatus().is5xxServerError()) {
            log.error(message, ex);
        } else if (errorType.getStatus().is4xxClientError()) {
            if (errorType == ErrorType.UNAUTHORIZED ||
                    errorType == ErrorType.FORBIDDEN ||
                    errorType == ErrorType.INVALID_CREDENTIALS) {
                log.warn(message);
            } else {
                log.debug(message);
            }
        } else {
            log.info(message);
        }
    }
}

