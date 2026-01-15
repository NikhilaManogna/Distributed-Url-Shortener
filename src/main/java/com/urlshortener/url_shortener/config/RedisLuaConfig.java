package com.urlshortener.url_shortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<List> globalRateLimitScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(
                new org.springframework.core.io.ClassPathResource("lua/global_rate_limit.lua")
        );
        script.setResultType(List.class);
        return script;
    }
}