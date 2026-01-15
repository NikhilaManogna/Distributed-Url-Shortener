package com.urlshortener.url_shortener.sequence;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGenerator {

    private static final String GLOBAL_COUNTER_KEY = "global:url:id";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisIdGenerator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long nextId() {
        Long id = redisTemplate.opsForValue().increment(GLOBAL_COUNTER_KEY);
        if (id == null) {
            throw new IllegalStateException("Failed to generate ID from Redis");
        }
        return id;
    }
}