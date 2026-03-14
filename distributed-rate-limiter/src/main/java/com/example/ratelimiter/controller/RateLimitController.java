package com.example.ratelimiter.controller;

import com.example.ratelimiter.service.DistributedRateLimiterService;
import com.example.ratelimiter.service.RateLimitResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class RateLimitController {

    private final DistributedRateLimiterService rateLimiterService;

    public RateLimitController(DistributedRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/resource")
    public ResponseEntity<Map<String, Object>> accessProtectedResource(
            @RequestHeader(name = "X-Client-Id") @NotBlank String clientId) {

        RateLimitResult result = rateLimiterService.isAllowed(clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));

        if (!result.allowed()) {
            headers.add("Retry-After", String.valueOf(result.retryAfterSeconds()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body(Map.of(
                            "allowed", false,
                            "message", "Rate limit exceeded. Try again later.",
                            "retryAfterSeconds", result.retryAfterSeconds()
                    ));
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of(
                        "allowed", true,
                        "message", "Request accepted by distributed rate limiter",
                        "remainingTokens", result.remainingTokens()
                ));
    }
}
