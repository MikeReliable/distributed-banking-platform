package com.mike.auth.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public boolean notAllowed(String key, int maxRequests, int windowSeconds) {
        if (maxRequests <= 0 || windowSeconds <= 0) {
            return false;
        }

        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowSeconds * 1000L;

        Deque<Long> timestamps = buckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }
}
