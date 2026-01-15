package com.urlshortener.url_shortener.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private Endpoint shorten = new Endpoint();
    private Endpoint redirect = new Endpoint();

    @Data
    public static class Endpoint {
        private int maxRequests;
        private int windowSeconds;
    }
}