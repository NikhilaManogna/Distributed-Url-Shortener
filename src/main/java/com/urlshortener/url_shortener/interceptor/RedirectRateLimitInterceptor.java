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
public class RedirectRateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> globalRateLimitScript;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        // Apply ONLY to redirect URLs
        String path = request.getRequestURI();
        if (path.startsWith("/api")) {
            return true;
        }

        String ip = request.getRemoteAddr();
        String key = "rate:redirect:" + path + ":" + ip;

        List<Long> result = redisTemplate.execute(
                globalRateLimitScript,
                List.of(key),
                String.valueOf(LIMIT),
                String.valueOf(WINDOW_SECONDS)
        );

        long current = result.get(0);
        long ttl = result.get(1);

        if (current > LIMIT) {
            throw new RateLimitExceededException("Too many redirect requests");
        }

        return true;
    }
}