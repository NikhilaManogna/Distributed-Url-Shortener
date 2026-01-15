package com.urlshortener.url_shortener.controller;

import com.urlshortener.url_shortener.dto.ShortenUrlRequest;
import com.urlshortener.url_shortener.dto.ShortenUrlResponse;
import com.urlshortener.url_shortener.dto.UrlStatsResponse;
import com.urlshortener.url_shortener.model.ShortUrl;
import com.urlshortener.url_shortener.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService service;

    @PostMapping("/shorten")
    public ShortenUrlResponse shortenUrl(@RequestBody ShortenUrlRequest request) {

        ShortUrl shortUrl = service.createShortUrl(
                request.getOriginalUrl(),
                request.getTtlSeconds()
        );

        return new ShortenUrlResponse(
                "http://localhost:8080/" + shortUrl.getShortCode()
        );
    }
}