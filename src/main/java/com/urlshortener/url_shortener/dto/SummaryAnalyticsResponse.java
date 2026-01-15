package com.urlshortener.url_shortener.dto;

import java.time.LocalDateTime;

public record SummaryAnalyticsResponse(
        String shortCode,
        long totalClicks,
        long todayClicks,
        long lastHourClicks,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}