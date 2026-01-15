package com.urlshortener.url_shortener.dto;

import java.util.Map;

public record HourlyClickStatsResponse(
        String shortCode,
        Map<String, Long> hourlyClicks
) {}