package com.urlshortener.url_shortener.service;

import com.urlshortener.url_shortener.dto.SummaryAnalyticsResponse;
import com.urlshortener.url_shortener.dto.TopUrlResponse;
import com.urlshortener.url_shortener.model.ShortUrl;
import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ShortUrlRepository repository;
    private final StringRedisTemplate redisTemplate;

    public List<TopUrlResponse> getTopUrls(int limit) {
        return repository
                .findTopUrls(PageRequest.of(0, limit))
                .stream()
                .map(row -> new TopUrlResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }

    public SummaryAnalyticsResponse getSummary(String shortCode) {

        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        long totalClicks = url.getClickCount();

        String todayKey = "click:day:" + shortCode + ":" + LocalDate.now();
        String hourKey = "click:hour:" + shortCode + ":" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));

        long todayClicks = getLong(todayKey);
        long hourClicks = getLong(hourKey);

        return new SummaryAnalyticsResponse(
                shortCode,
                totalClicks + todayClicks, // include unsynced Redis clicks
                todayClicks,
                hourClicks,
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }

    private long getLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }
}