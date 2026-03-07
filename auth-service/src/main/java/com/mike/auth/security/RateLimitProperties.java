package com.mike.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.rate-limit")
public class RateLimitProperties {

    private Limit login = new Limit(20, 60);
    private Limit token = new Limit(40, 60);

    @Getter
    @Setter
    public static class Limit {
        private int maxRequests;
        private int windowSeconds;

        public Limit() {
        }

        public Limit(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
}
