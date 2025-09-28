package com.madrasati.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.*;

@Service
public class IdentityService {

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(boolean success, String token, String message) {}

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LoginResponse loginWithRetry(LoginRequest req, int maxRetries, Duration perAttemptTimeout, Duration initialBackoff) {
        int attempt = 0;
        Duration backoff = initialBackoff;
        Exception lastEx = null;

        while (attempt <= maxRetries) {
            attempt++;
            try {
                return attemptLoginWithTimeout(req, perAttemptTimeout);
            } catch (Exception e) {
                lastEx = e;
                if (attempt > maxRetries) break;
                try { Thread.sleep(backoff.toMillis()); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                backoff = backoff.multipliedBy(2).minusMillis(0); // simple exponential backoff
            }
        }
        return new LoginResponse(false, null, "Login failed after retries: " + (lastEx != null ? lastEx.getMessage() : "unknown"));
    }

    private LoginResponse attemptLoginWithTimeout(LoginRequest req, Duration timeout) throws Exception {
        CompletableFuture<LoginResponse> future = CompletableFuture.supplyAsync(() -> simulateAuth(req));
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            throw new RuntimeException("Auth timed out");
        }
    }

    private LoginResponse simulateAuth(LoginRequest req) {
        // Simulate variable latency and occasional failures
        try { Thread.sleep(100 + (long)(Math.random() * 300)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (Math.random() < 0.2) { // 20% failure to trigger retries
            throw new RuntimeException("Transient identity provider error");
        }
        if ("student".equals(req.username()) && "secret".equals(req.password())) {
            return new LoginResponse(true, "fake-jwt-token", "ok");
        }
        return new LoginResponse(false, null, "invalid credentials");
    }
}
