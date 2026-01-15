package com.urlshortener.url_shortener.ratelimit;

import com.urlshortener.url_shortener.dto.RateLimitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> rateLimitLuaScript;

    public RateLimitResult consume(String key, int limit, int windowSeconds) {

        long now = Instant.now().getEpochSecond();

        List<Long> result = redisTemplate.execute(
                rateLimitLuaScript,
                List.of(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds),
                String.valueOf(now)
        );

        if (result == null || result.size() < 3) {
            return new RateLimitResult(true, limit, limit, now + windowSeconds);
        }

        long current = result.get(0);
        long remaining = result.get(1);
        long resetAtEpoch = result.get(2);
        long resetInSeconds = Math.max(0, resetAtEpoch - now);

        boolean allowed = current <= limit;

        return new RateLimitResult(
                allowed,
                limit,
                remaining,
                resetInSeconds
        );
    }
}