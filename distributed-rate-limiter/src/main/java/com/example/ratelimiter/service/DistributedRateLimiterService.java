package com.example.ratelimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributedRateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> tokenBucketScript;
    private final long defaultCapacity;
    private final long defaultRefillTokens;
    private final long defaultRefillPeriodSeconds;

    public DistributedRateLimiterService(RedisTemplate<String, String> redisTemplate,
                                         RedisScript<List> tokenBucketScript,
                                         @Value("${rate-limiter.default-capacity:20}") long defaultCapacity,
                                         @Value("${rate-limiter.default-refill-tokens:20}") long defaultRefillTokens,
                                         @Value("${rate-limiter.default-refill-period-seconds:60}") long defaultRefillPeriodSeconds) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillTokens = defaultRefillTokens;
        this.defaultRefillPeriodSeconds = defaultRefillPeriodSeconds;
    }

    public RateLimitResult isAllowed(String clientId) {
        String bucketKey = "rate-limit:bucket:" + clientId;
        String timestampKey = "rate-limit:ts:" + clientId;

        List result = redisTemplate.execute(
                tokenBucketScript,
                List.of(bucketKey, timestampKey),
                String.valueOf(defaultCapacity),
                String.valueOf(defaultRefillTokens),
                String.valueOf(defaultRefillPeriodSeconds),
                "1"
        );

        if (result == null || result.size() < 3) {
            return new RateLimitResult(false, 0, defaultRefillPeriodSeconds);
        }

        long allowed = Long.parseLong(result.get(0).toString());
        long remainingTokens = Long.parseLong(result.get(1).toString());
        long retryAfterSeconds = Long.parseLong(result.get(2).toString());

        return new RateLimitResult(allowed == 1, remainingTokens, retryAfterSeconds);
    }
}
