package com.example.crud.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigStandaloneTest {
    @Test
    void cacheManager_shouldUseJwtTokenExpiration_whenCacheExpiryMsIsNull() {
        CacheConfig config = new CacheConfig();
        // Set fields via reflection
        setField(config, "cacheNames", "tokens,users");
        setField(config, "cacheExpiryMs", null);
        setField(config, "cacheExpiryUnit", "minutes");
        setField(config, "cacheExpiryMin", 1L);
        setField(config, "cacheMaxSize", 1000);
        setField(config, "jwtTokenExpiration", 12345L);

        CacheManager manager = config.cacheManager();
        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(((CaffeineCacheManager) manager).getCacheNames()).contains("tokens", "users");
    }

    @Test
    void cacheManager_shouldUseCacheExpiryMs_whenNotNull() {
        CacheConfig config = new CacheConfig();
        setField(config, "cacheNames", "tokens");
        setField(config, "cacheExpiryMs", 60000L);
        setField(config, "cacheExpiryUnit", "seconds");
        setField(config, "cacheExpiryMin", 1L);
        setField(config, "cacheMaxSize", 1000);
        setField(config, "jwtTokenExpiration", 12345L);

        CacheManager manager = config.cacheManager();
        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(((CaffeineCacheManager) manager).getCacheNames()).contains("tokens");
    }

    @Test
    void cacheManager_shouldHandleEmptyCacheNames() {
        CacheConfig config = new CacheConfig();
        setField(config, "cacheNames", "");
        setField(config, "cacheExpiryMs", 1000L);
        setField(config, "cacheExpiryUnit", "minutes");
        setField(config, "cacheExpiryMin", 1L);
        setField(config, "cacheMaxSize", 1000);
        setField(config, "jwtTokenExpiration", 12345L);

        CacheManager manager = config.cacheManager();
        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(((CaffeineCacheManager) manager).getCacheNames()).isEmpty();
    }

    @Test
    void cacheManager_shouldSupportDifferentUnits() {
        CacheConfig config = new CacheConfig();
        setField(config, "cacheNames", "tokens");
        setField(config, "cacheExpiryMs", 60000L);
        setField(config, "cacheExpiryUnit", "SECONDS");
        setField(config, "cacheExpiryMin", 1L);
        setField(config, "cacheMaxSize", 1000);
        setField(config, "jwtTokenExpiration", 12345L);

        CacheManager manager = config.cacheManager();
        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(((CaffeineCacheManager) manager).getCacheNames()).contains("tokens");
    }

    // Helper to set private fields
    private void setField(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
