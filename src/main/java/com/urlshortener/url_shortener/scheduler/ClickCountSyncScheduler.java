package com.urlshortener.url_shortener.scheduler;

import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickCountSyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final ShortUrlRepository shortUrlRepository;

    @Scheduled(fixedRate = 60000) // every 60 seconds
    public void syncClicksToDatabase() {

        Set<String> keys = redisTemplate.keys("click:count:*");

        if (keys == null || keys.isEmpty()) {
            log.info("ℹ No click keys found");
            return;
        }

        for (String key : keys) {
            String shortCode = key.replace("click:count:", "");
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) continue;

            long clicks = Long.parseLong(value);

            shortUrlRepository.incrementClickCount(shortCode, clicks);

            redisTemplate.delete(key);

            log.info("✅ Synced {} clicks for {}", clicks, shortCode);
        }
    }
}