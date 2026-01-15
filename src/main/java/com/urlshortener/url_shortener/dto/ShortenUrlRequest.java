package com.urlshortener.url_shortener.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenUrlRequest {
    private String originalUrl;
    private Long ttlSeconds; // seconds
}