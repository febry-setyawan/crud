package com.example.crud.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CacheConfig {
    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${cache.maxSize:10000}")
    private int cacheMaxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("refreshTokens", "users", "roles");
        // Konversi jwtExpirationMs (ms) ke menit, minimal 1 menit
        long expiryMinutes = Math.max(1, jwtExpirationMs / 60000);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expiryMinutes, TimeUnit.MINUTES)
                .maximumSize(cacheMaxSize));
        return cacheManager;
    }
}
