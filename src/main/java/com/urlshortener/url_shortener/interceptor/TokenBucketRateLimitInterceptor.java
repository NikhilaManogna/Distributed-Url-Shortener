package com.urlshortener.url_shortener.interceptor;

import com.urlshortener.url_shortener.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//@Component
@RequiredArgsConstructor
public class TokenBucketRateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_TOKENS = 100;
    private static final int REFILL_RATE_PER_SECOND = 2;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String ip = request.getRemoteAddr();
        String tokenKey = "bucket:" + ip + ":tokens";
        String timeKey = "bucket:" + ip + ":lastRefill";

        long now = System.currentTimeMillis();

        Long tokens = redisTemplate.opsForValue().get(tokenKey) != null
                ? Long.parseLong(redisTemplate.opsForValue().get(tokenKey))
                : MAX_TOKENS;

        Long lastRefill = redisTemplate.opsForValue().get(timeKey) != null
                ? Long.parseLong(redisTemplate.opsForValue().get(timeKey))
                : now;

        long elapsedSeconds = (now - lastRefill) / 1000;
        long refillTokens = elapsedSeconds * REFILL_RATE_PER_SECOND;

        tokens = Math.min(MAX_TOKENS, tokens + refillTokens);

        if (tokens <= 0) {
            throw new RateLimitExceededException("Too many requests (token bucket)");
        }

        tokens--;

        redisTemplate.opsForValue().set(tokenKey, String.valueOf(tokens));
        redisTemplate.opsForValue().set(timeKey, String.valueOf(now));

        return true;
    }
}