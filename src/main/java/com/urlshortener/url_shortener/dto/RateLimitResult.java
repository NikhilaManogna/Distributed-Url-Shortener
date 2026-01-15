package com.urlshortener.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitResult {
    private boolean allowed;
    private long limit;
    private long remaining;
    private long resetSeconds;
}