package com.urlshortener.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DailyClickStatsResponse {
    private String shortCode;
    private Map<String, Long> dailyClicks;
}