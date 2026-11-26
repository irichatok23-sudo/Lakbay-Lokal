package com.lakbaylokal.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe sliding window rate limiter to protect authentication endpoints
 * against brute force attacks and credential stuffing.
 */
public class RateLimiter {

    private static class AttemptHistory {
        private int count;
        private long windowStart;

        AttemptHistory(long now) {
            this.count = 1;
            this.windowStart = now;
        }

        synchronized boolean recordAttempt(int maxAttempts, long windowMs, long now) {
            if (now - windowStart > windowMs) {
                // Reset window
                windowStart = now;
                count = 1;
                return true;
            }
            if (count >= maxAttempts) {
                return false;
            }
            count++;
            return true;
        }

        synchronized long getRemainingLockoutSeconds(long windowMs, long now) {
            long remaining = (windowStart + windowMs) - now;
            return remaining > 0 ? (remaining / 1000) + 1 : 0;
        }
    }

    private final Map<String, AttemptHistory> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long windowMs;

    public RateLimiter(int maxAttempts, long windowMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    /**
     * Checks if the client key is permitted. Increments failure count if recorded.
     * @return true if permitted, false if rate limited.
     */
    public boolean allow(String key) {
        if (key == null || key.isBlank()) return true;
        long now = System.currentTimeMillis();
        AttemptHistory history = attempts.computeIfAbsent(key, k -> new AttemptHistory(now));
        return history.recordAttempt(maxAttempts, windowMs, now);
    }

    /**
     * Resets rate limiter history on successful authentication.
     */
    public void reset(String key) {
        if (key != null) {
            attempts.remove(key);
        }
    }

    /**
     * Returns seconds remaining until lockout window resets.
     */
    public long getLockoutSeconds(String key) {
        if (key == null) return 0;
        AttemptHistory history = attempts.get(key);
        if (history == null) return 0;
        return history.getRemainingLockoutSeconds(windowMs, System.currentTimeMillis());
    }
}
