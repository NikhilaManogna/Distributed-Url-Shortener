package com.urlshortener.url_shortener.interceptor;

import com.urlshortener.url_shortener.dto.RateLimitResult;
import com.urlshortener.url_shortener.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

//@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> rateLimitLuaScript;

    private static final int RATE_LIMIT = 200;
    private static final int WINDOW_SECONDS = 10;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        String path = request.getRequestURI();

        if (!path.matches("^/[a-zA-Z0-9]+$")) {
            return true;
        }

        String ip = request.getRemoteAddr();
        String shortCode = path.substring(1);

        String key = "rate:url:" + ip + ":" + shortCode;

        long now = Instant.now().getEpochSecond();

        List<Long> result = redisTemplate.execute(
                rateLimitLuaScript,
                List.of(key),
                String.valueOf(RATE_LIMIT),
                String.valueOf(WINDOW_SECONDS),
                String.valueOf(now)
        );

        long current = result.get(0);
        long remaining = result.get(1);
        long reset = result.get(2);

        response.setHeader("X-RateLimit-Limit", String.valueOf(RATE_LIMIT));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(reset));

        if (current > RATE_LIMIT) {
            throw new RateLimitExceededException(
                    "Too many requests for this short URL. Try again later."
            );
        }
        return true;
    }
}