package com.urlshortener.url_shortener.scheduler;

import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ExpiredUrlCleanupScheduler {

    private final ShortUrlRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    public ExpiredUrlCleanupScheduler(
            ShortUrlRepository repository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void cleanup() {

        log.info("🧹 Running expired URL cleanup");

        LocalDateTime now = LocalDateTime.now();

        // 1️⃣ Fetch expired short codes FIRST
        List<String> expiredCodes = repository.findExpiredShortCodes(now);

        if (expiredCodes.isEmpty()) {
            log.info("ℹ No expired URLs found");
            return;
        }

        // 2️⃣ Delete from DB
        int deleted = repository.deleteExpired(now);

        // 3️⃣ Evict Redis cache
        expiredCodes.forEach(code -> {
            redisTemplate.delete("url:" + code);
            redisTemplate.delete("click:count:" + code);
        });

        log.info("✅ Cleanup completed — {} expired URLs removed", deleted);
    }
}