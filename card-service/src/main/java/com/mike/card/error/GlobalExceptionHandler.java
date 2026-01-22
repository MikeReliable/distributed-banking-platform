package com.mike.card.error;

import com.mike.card.common.ApiError;
import com.mike.card.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import static com.mike.card.error.ErrorType.VALIDATION_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

        return ResponseEntity.badRequest().body(
                buildError(
                        VALIDATION_ERROR.name(),
                        "Validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        detail,
                        request
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                buildError(
                        VALIDATION_ERROR.name(),
                        "Validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        request
                )
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

    private ApiError buildError(
            String type,
            String title,
            int status,
            String detail,
            HttpServletRequest request
    ) {
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            requestId = "N/A";
        }
        return new ApiError(
                type,
                title,
                status,
                detail,
                request.getRequestURI(),
                requestId,
                Instant.now()
        );
    }
}
