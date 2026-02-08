package com.mike.transfer.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

    @Bean
    public RequestInterceptor requestIdFeignInterceptor() {
        return template -> {
            String requestId = MDC.get("requestId");
            if (requestId != null) {
                template.header("X-Request-Id", requestId);
            }
        };
    }
}
