package com.urlshortener.url_shortener.interceptor;

import com.urlshortener.url_shortener.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GlobalRateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT = 3;
    private static final int WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> globalRateLimitScript;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        String key = "rate:limit:" + path + ":" + ip;

        List<Long> result = redisTemplate.execute(
                globalRateLimitScript,
                List.of(key),
                String.valueOf(LIMIT),
                String.valueOf(WINDOW_SECONDS)
        );

        long current = result.get(0);
        long ttl = result.get(1);

        long remaining = Math.max(0, LIMIT - current);

        response.setHeader("X-RateLimit-Limit", String.valueOf(LIMIT));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(
                ttl > 0 ? ttl : WINDOW_SECONDS
        ));

        if (current > LIMIT) {
            throw new RateLimitExceededException("Too many requests");
        }

        return true;
    }
}