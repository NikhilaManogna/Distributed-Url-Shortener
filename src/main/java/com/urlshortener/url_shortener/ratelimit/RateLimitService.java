package com.urlshortener.url_shortener.ratelimit;

import com.urlshortener.url_shortener.dto.RateLimitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisRateLimiter redisRateLimiter;

    /**
     * Validates and consumes a rate limit token.
     *
     * @param key unique rate limit key (ip + endpoint)
     * @param maxRequests allowed requests in window
     * @param duration window duration
     */
    public RateLimitResult validate(
            String key,
            int maxRequests,
            Duration duration
    ) {
        return redisRateLimiter.consume(
                key,
                maxRequests,
                (int) duration.getSeconds()
        );
    }
}