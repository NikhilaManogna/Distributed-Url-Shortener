package com.urlshortener.url_shortener.service;

import com.urlshortener.url_shortener.dto.UrlStatsResponse;
import com.urlshortener.url_shortener.exception.RateLimitExceededException;
import com.urlshortener.url_shortener.exception.ShortUrlNotFoundException;
import com.urlshortener.url_shortener.exception.UrlExpiredException;
import com.urlshortener.url_shortener.model.ShortUrl;
import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import com.urlshortener.url_shortener.util.Base62Encoder;
import org.springframework.data.redis.core.RedisTemplate;
import com.urlshortener.url_shortener.sequence.RedisIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShortUrlService {

    private static final long CACHE_TTL_SECONDS = 86400;
    private final Base62Encoder base62Encoder;
    private final RedisIdGenerator redisIdGenerator;

    private final ShortUrlRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    public ShortUrlService(
            ShortUrlRepository repository,
            RedisTemplate<String, String> redisTemplate,
            Base62Encoder base62Encoder,
            RedisIdGenerator redisIdGenerator
    ) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.base62Encoder = base62Encoder;
        this.redisIdGenerator = redisIdGenerator;
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, Long ttlSeconds) {

        log.info("USING REDIS ID + BASE62 FLOW");

        long id = redisIdGenerator.nextId();
        String shortCode = base62Encoder.encode(id);

        LocalDateTime expiresAt = null;

        if (ttlSeconds != null && ttlSeconds > 0) {
            expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        }

        ShortUrl url = ShortUrl.builder()
                .id(id)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        repository.save(url);

        redisTemplate.opsForValue().set(
                "url:" + shortCode,
                originalUrl,
                CACHE_TTL_SECONDS,
                TimeUnit.SECONDS
        );

        return url;
    }

    public String getOriginalUrlAndIncrement(String shortCode) {

        String cacheKey = "url:" + shortCode;

        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            incrementClick(shortCode);
            return cachedUrl;
        }

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));

        if (shortUrl.getExpiresAt() != null &&
                shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("Short URL expired");
        }

        if (shortUrl.getExpiresAt() != null) {
            long ttlSeconds = Duration.between(
                    LocalDateTime.now(),
                    shortUrl.getExpiresAt()
            ).getSeconds();

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(
                        cacheKey,
                        shortUrl.getOriginalUrl(),
                        ttlSeconds,
                        TimeUnit.SECONDS
                );
            }
        } else {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    shortUrl.getOriginalUrl(),
                    CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
        }

        incrementClick(shortCode);
        return shortUrl.getOriginalUrl();
    }

    private void incrementClick(String shortCode) {

        // total clicks
        String totalKey = "click:count:" + shortCode;
        redisTemplate.opsForValue().increment(totalKey);

        // daily clicks
        String day = LocalDate.now().toString(); // YYYY-MM-DD
        String dailyKey = "click:day:" + shortCode + ":" + day;

        redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, 7, TimeUnit.DAYS);

        String hour = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));

        String hourlyKey = "click:hour:" + shortCode + ":" + hour;

        redisTemplate.opsForValue().increment(hourlyKey);
        redisTemplate.expire(hourlyKey, 2, TimeUnit.DAYS);

    }

    public UrlStatsResponse getStats(String shortCode) {

        ShortUrl url = repository.findByShortCode(shortCode).orElse(null);

        String clickKey = "click:count:" + shortCode;
        Long redisClicks = 0L;

        Object redisValue = redisTemplate.opsForValue().get(clickKey);
        if (redisValue != null) {
            redisClicks = Long.valueOf(redisValue.toString());
        }

        // DB row missing but Redis still has data
        if (url == null) {
            String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);
            if (cachedUrl == null) {
                throw new ShortUrlNotFoundException("Short URL not found");
            }

            return new UrlStatsResponse(
                    shortCode,
                    cachedUrl,
                    redisClicks,
                    null
            );
        }

        return new UrlStatsResponse(
                shortCode,
                url.getOriginalUrl(),
                url.getClickCount() + redisClicks,
                url.getCreatedAt()
        );
    }

    public ShortUrl getAndValidateShortUrl(String shortCode) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));

        if (shortUrl.getExpiresAt() != null &&
                shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new UrlExpiredException("Short URL expired");
        }

        return shortUrl;
    }
}