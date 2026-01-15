package com.urlshortener.url_shortener.controller;

import com.urlshortener.url_shortener.dto.DailyClickStatsResponse;
import com.urlshortener.url_shortener.dto.HourlyClickStatsResponse;
import com.urlshortener.url_shortener.dto.SummaryAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import com.urlshortener.url_shortener.service.AnalyticsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Tag(name = "Analytics", description = "Click analytics APIs")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final StringRedisTemplate stringRedisTemplate;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}/daily")
    public DailyClickStatsResponse getDailyStats(@PathVariable String shortCode) {

        String pattern = "click:day:" + shortCode + ":*";
        Set<String> keys = stringRedisTemplate.keys(pattern);

        Map<String, Long> dailyClicks = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                String date = key.substring(key.lastIndexOf(":") + 1);
                String value = stringRedisTemplate.opsForValue().get(key);
                if (value != null) {
                    dailyClicks.put(date, Long.parseLong(value));
                }
            }
        }

        return new DailyClickStatsResponse(shortCode, dailyClicks);
    }

    @GetMapping("/top")
    public Map<String, Object> topUrls(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return Map.of(
                "topUrls",
                analyticsService.getTopUrls(limit)
        );
    }

    @GetMapping("/{shortCode}/hourly")
    public HourlyClickStatsResponse getHourlyStats(
            @PathVariable String shortCode
    ) {
        String pattern = "click:hour:" + shortCode + ":*";
        Set<String> keys = stringRedisTemplate.keys(pattern);

        Map<String, Long> hourlyClicks = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                String hour = key.substring(key.lastIndexOf(":") + 1);
                String value = stringRedisTemplate.opsForValue().get(key);
                if (value != null) {
                    hourlyClicks.put(hour, Long.parseLong(value));
                }
            }
        }

        return new HourlyClickStatsResponse(shortCode, hourlyClicks);
    }

    @Operation(summary = "Get summary analytics for a short URL")
    @GetMapping("/{shortCode}/summary")
    public SummaryAnalyticsResponse summary(@PathVariable String shortCode) {
        return analyticsService.getSummary(shortCode);
    }

}