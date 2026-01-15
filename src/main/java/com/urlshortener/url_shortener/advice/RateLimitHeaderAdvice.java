package com.urlshortener.url_shortener.advice;

import com.urlshortener.url_shortener.dto.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@Component
@RequiredArgsConstructor
public class RateLimitHeaderAdvice implements ResponseBodyAdvice<Object> {

    private final HttpServletRequest request;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest req,
                                  ServerHttpResponse res) {

        Object obj = request.getAttribute("rateLimitResult");

        if (obj instanceof RateLimitResult result) {
            res.getHeaders().add("X-RateLimit-Limit",
                    String.valueOf(result.getLimit()));
            res.getHeaders().add("X-RateLimit-Remaining",
                    String.valueOf(result.getRemaining()));
            res.getHeaders().add("X-RateLimit-Reset",
                    String.valueOf(result.getResetSeconds()));
        }

        return body;
    }
}