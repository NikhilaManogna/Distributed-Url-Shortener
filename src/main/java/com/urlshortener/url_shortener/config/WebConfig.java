package com.urlshortener.url_shortener.config;

import com.urlshortener.url_shortener.interceptor.GlobalRateLimitInterceptor;
import com.urlshortener.url_shortener.interceptor.RedirectRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GlobalRateLimitInterceptor globalRateLimitInterceptor;
    private final RedirectRateLimitInterceptor redirectRateLimitInterceptor;

    public WebConfig(
            GlobalRateLimitInterceptor globalRateLimitInterceptor,
            RedirectRateLimitInterceptor redirectRateLimitInterceptor
    ) {
        this.globalRateLimitInterceptor = globalRateLimitInterceptor;
        this.redirectRateLimitInterceptor = redirectRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Rate limit API endpoints (/api/**)
        registry.addInterceptor(globalRateLimitInterceptor)
                .addPathPatterns("/api/**");

        // Rate limit redirect endpoints (/{shortCode})
        registry.addInterceptor(redirectRateLimitInterceptor)
                .addPathPatterns("/**");
    }
}
