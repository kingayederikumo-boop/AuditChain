package com.auditchain.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-key fixed-window rate limiter. Prototype-grade; not distributed.
 */
@Component
public class SimpleRateLimiter {

    private final int maxPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public SimpleRateLimiter(@Value("${auditchain.rate-limit.per-minute:60}") int maxPerMinute) {
        this.maxPerMinute = Math.max(1, maxPerMinute);
    }

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMs >= 60_000) {
                return new Window(now, new AtomicInteger(0));
            }
            return existing;
        });
        return w.count.incrementAndGet() <= maxPerMinute;
    }

    private static final class Window {
        final long startMs;
        final AtomicInteger count;
        Window(long startMs, AtomicInteger count) {
            this.startMs = startMs;
            this.count = count;
        }
    }
}
