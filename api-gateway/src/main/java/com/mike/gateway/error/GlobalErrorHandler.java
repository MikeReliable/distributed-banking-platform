package com.mike.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.gateway.common.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private static class ErrorInfo {
        final ErrorType errorType;
        final String detail;

        ErrorInfo(ErrorType errorType, String detail) {
            this.errorType = errorType;
            this.detail = detail;
        }
    }

    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        return Mono.deferContextual(ctx -> {
            String requestId = resolveRequestId(exchange, ctx);

            ErrorInfo errorInfo;
            if (ex instanceof ResponseStatusException) {
                errorInfo = resolveResponseStatusErrorInfo((ResponseStatusException) ex);
            } else {
                errorInfo = resolveErrorInfo(ex);
            }

            logError(exchange, requestId, errorInfo, ex);

            return writeErrorResponse(
                    exchange,
                    errorInfo.errorType.getStatus(),
                    errorInfo.errorType.name(),
                    resolveTitle(errorInfo.errorType.getStatus()),
                    errorInfo.detail,
                    ctx
            );
        });
    }

    private ErrorInfo resolveErrorInfo(Throwable ex) {
        if (ex instanceof org.springframework.cloud.gateway.support.TimeoutException) {
            return new ErrorInfo(ErrorType.GATEWAY_TIMEOUT, "Upstream service timeout");
        }
        if (ex instanceof ConnectException) {
            return new ErrorInfo(ErrorType.SERVICE_UNAVAILABLE, "Upstream service unavailable");
        }
        if (ex instanceof SocketTimeoutException) {
            return new ErrorInfo(ErrorType.SERVICE_UNAVAILABLE, "Upstream service timeout");
        }
        if (ex instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            return new ErrorInfo(ErrorType.AUTHENTICATION_ERROR, "Authentication failed");
        }
        return new ErrorInfo(ErrorType.INTERNAL_ERROR, "Unexpected error");
    }

    private ErrorInfo resolveResponseStatusErrorInfo(ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String detail = ex.getReason() != null ? ex.getReason() : "Client error";

        ErrorType errorType;
        if (statusCode.value() == HttpStatus.UNAUTHORIZED.value()) {
            errorType = ErrorType.AUTHENTICATION_ERROR;
        } else {
            errorType = ErrorType.CLIENT_ERROR;
        }

        return new ErrorInfo(errorType, detail);
    }

    private void logError(ServerWebExchange exchange, String requestId, ErrorInfo errorInfo, Throwable ex) {
        String message = String.format(
                "[requestId=%s] %s %s | status=%d | type=%s | detail=%s",
                requestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                errorInfo.errorType.getStatus().value(),
                errorInfo.errorType.name(),
                errorInfo.detail
        );

        if (errorInfo.errorType.getStatus().is5xxServerError()) {
            log.error(message, ex);
        } else if (errorInfo.errorType.getStatus().is4xxClientError()) {
            if (errorInfo.errorType == ErrorType.AUTHENTICATION_ERROR) {
                log.warn(message);
            } else {
                log.debug(message);
            }
        } else {
            log.info(message);
        }
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
                                          String type, String title, String detail,
                                          ContextView ctx) {
        ApiError error = GatewayErrorBuilder.build(
                type,
                title,
                status,
                detail,
                exchange,
                ctx
        );

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(error);
        } catch (Exception e) {
            log.error("[requestId={}] Failed to serialize error response",
                    ctx.getOrDefault("requestId", "N/A"), e);
            return Mono.error(e);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String resolveRequestId(ServerWebExchange exchange, ContextView ctx) {
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null && ctx.hasKey("requestId")) {
            requestId = ctx.get("requestId");
        }
        if (requestId == null) {
            requestId = String.valueOf(java.util.UUID.randomUUID());
        }
        return requestId;
    }

    private String resolveTitle(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case TOO_MANY_REQUESTS -> "Too Many Requests";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            case SERVICE_UNAVAILABLE -> "Service Unavailable";
            case GATEWAY_TIMEOUT -> "Gateway Timeout";
            default -> "Error";
        };
    }
}