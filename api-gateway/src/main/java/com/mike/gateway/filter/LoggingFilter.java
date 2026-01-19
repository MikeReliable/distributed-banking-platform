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

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().toString();

        var ref = new Object() {
            String requestId = exchange.getAttribute(CorrelationIdFilter.HEADER);
        };
        if (ref.requestId == null) ref.requestId = "N/A";

        log.info("[requestId={}] Incoming {} {}", ref.requestId, method, path);

        return chain.filter(exchange)
                .doOnSuccess(unused ->
                        log.info("[requestId={}] Completed {}",
                                ref.requestId,
                                exchange.getResponse().getStatusCode()
                        )
                );
    }
}
