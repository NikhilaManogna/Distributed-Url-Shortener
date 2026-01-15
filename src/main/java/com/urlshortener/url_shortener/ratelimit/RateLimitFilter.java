package com.urlshortener.url_shortener.ratelimit;

import com.urlshortener.url_shortener.dto.RateLimitResult;
import com.urlshortener.url_shortener.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 🚨 Skip non-POST /api/shorten
        if (uri.equals("/api/shorten") && !"POST".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🚨 Skip non-API routes
        if (!uri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (uri.startsWith("/api/analytics")) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitProperties.Endpoint config =
                uri.startsWith("/api/shorten")
                        ? rateLimitProperties.getShorten()
                        : rateLimitProperties.getRedirect();

        String ip = request.getRemoteAddr();
        String key = "rate:limit:" + uri + ":" + ip;

        RateLimitResult result = rateLimitService.validate(
                key,
                config.getMaxRequests(),
                Duration.ofSeconds(config.getWindowSeconds())
        );

        // ✅ Headers ONLY for rate-limited endpoints
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        if (!result.isAllowed()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
    {
      "error": "Too Many Requests",
      "message": "Rate limit exceeded. Please try again later."
    }
    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}