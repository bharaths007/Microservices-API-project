package com.example.ratelimiter.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistributedRateLimiterServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void shouldAllowWhenScriptReturnsAllowed() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        RedisScript<List> script = mock(RedisScript.class);

        when(redisTemplate.execute(eq(script), any(List.class), any()))
                .thenReturn(List.of(1L, 7L, 0L));

        DistributedRateLimiterService service = new DistributedRateLimiterService(redisTemplate, script, 10, 10, 60);

        RateLimitResult result = service.isAllowed("client-a");

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingTokens()).isEqualTo(7L);
        assertThat(result.retryAfterSeconds()).isZero();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldDenyWhenScriptReturnsRejected() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        RedisScript<List> script = mock(RedisScript.class);

        when(redisTemplate.execute(eq(script), any(List.class), any()))
                .thenReturn(List.of(0L, 0L, 12L));

        DistributedRateLimiterService service = new DistributedRateLimiterService(redisTemplate, script, 10, 10, 60);

        RateLimitResult result = service.isAllowed("client-b");

        assertThat(result.allowed()).isFalse();
        assertThat(result.remainingTokens()).isZero();
        assertThat(result.retryAfterSeconds()).isEqualTo(12L);
    }
}
