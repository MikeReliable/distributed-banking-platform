package com.mike.user.error;

import com.mike.user.common.ApiError;
import com.mike.user.common.ApiException;
import com.mike.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "[requestId={}] User not found | path={} | msg={}",
                resolveRequestId(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildError(
                        ErrorType.USER_NOT_FOUND.name(),
                        "Not Found",
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        request
                )
        );
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

        log.warn(
                "[requestId={}] Validation error | path={} | detail={}",
                resolveRequestId(),
                request.getRequestURI(),
                detail
        );

        return badRequest(ErrorType.VALIDATION_ERROR.name(), detail, request);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Business error | type={} | path={} | msg={}",
                ex.getType(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatus())
                .body(buildError(
                        ex.getType(),
                        ex.getMessage(),
                        ex.getStatus(),
                        ex.getMessage(),
                        request
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "[requestId={}] Unexpected error | path={}",
                resolveRequestId(),
                request.getRequestURI(),
                ex
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(
                        ErrorType.INTERNAL_ERROR.name(),
                        "Internal error",
                        500,
                        "Unexpected error",
                        request
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "[requestId={}] Validation error | path={} | msg={}",
                resolveRequestId(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return badRequest(
                ErrorType.VALIDATION_ERROR.name(),
                ex.getMessage(),
                request
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

