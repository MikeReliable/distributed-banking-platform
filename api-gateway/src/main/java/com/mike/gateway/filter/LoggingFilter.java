package com.mike.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class LoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        return Mono.deferContextual(ctx -> {
            String requestId = ctx.getOrDefault(
                    CorrelationIdFilter.CONTEXT_KEY,
                    "N/A"
            );

            log.info("[requestId={}] Incoming {} {}",
                    requestId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath()
            );

            return chain.filter(exchange)
                    .doOnSuccess(v ->
                            log.info("[requestId={}] Completed {}",
                                    requestId,
                                    exchange.getResponse().getStatusCode()
                            )
                    )
                    .doOnError(e ->
                            log.error("Failed [requestId={}]: {}",
                                    requestId,
                                    e.getMessage()
                            )
                    );
        });
    }
}
