package com.madrasati.services;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple Circuit Breaker implementation for guarding calls to the Exam Service.
 */
public class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private volatile State state = State.CLOSED;
    private final int failureThreshold;
    private final Duration openStateDuration;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile Instant openSince = null;

    public CircuitBreaker(int failureThreshold, Duration openStateDuration) {
        this.failureThreshold = failureThreshold;
        this.openStateDuration = openStateDuration;
    }

    public synchronized <T> T call(Callable<T> action) throws Exception {
        if (state == State.OPEN) {
            if (Instant.now().isAfter(openSince.plus(openStateDuration))) {
                state = State.HALF_OPEN; // Try one request
            } else {
                throw new IllegalStateException("Circuit is OPEN; rejecting calls");
            }
        }

        try {
            T result = action.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private void onSuccess() {
        if (state == State.HALF_OPEN) {
            // success on HALF_OPEN closes it
            state = State.CLOSED;
            consecutiveFailures.set(0);
        } else if (state == State.CLOSED) {
            consecutiveFailures.set(0);
        }
    }

    private void onFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (state == State.HALF_OPEN || (state == State.CLOSED && failures >= failureThreshold)) {
            state = State.OPEN;
            openSince = Instant.now();
        }
    }

    public State getState() { return state; }
}
