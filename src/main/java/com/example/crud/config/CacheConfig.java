package com.example.crud.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

@Configuration

public class CacheConfig {
    @Value("${cache.names:tokens}")
    private String cacheNames;

    @Value("${cache.expiry.ms:#{null}}")
    private Long cacheExpiryMs;

    @Value("${cache.expiry.unit:minutes}")
    private String cacheExpiryUnit;

    @Value("${cache.expiry.min:1}")
    private long cacheExpiryMin;

    @Value("${cache.maxsize:10000}")
    private int cacheMaxSize;

    @Value("${jwt.token.expiration:3600000}")
    private long jwtTokenExpiration;

    @Bean
    public CacheManager cacheManager() {
        String[] names = Arrays.stream(cacheNames.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(names);

        // Hitung expiry dari property cache.expiry.ms, fallback ke jwt.expirationMs jika null
        long expiryMs = cacheExpiryMs != null ? cacheExpiryMs : jwtTokenExpiration;
        TimeUnit unit = TimeUnit.valueOf(cacheExpiryUnit.toUpperCase());
        long expiryValue = Math.max(cacheExpiryMin, unit.convert(expiryMs, TimeUnit.MILLISECONDS));

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expiryValue, unit)
                .maximumSize(cacheMaxSize));
        return cacheManager;
    }
}
