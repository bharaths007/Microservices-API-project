package com.example.ratelimiter.service;

public record RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {
}
