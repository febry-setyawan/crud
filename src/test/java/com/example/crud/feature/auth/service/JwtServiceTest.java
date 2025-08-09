package com.example.crud.feature.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtServiceTest {
    @Test
    void generateRefreshToken_shouldNotThrow_whenCacheIsNull() {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("refreshTokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        String token = service.generateRefreshToken("user");
        assertThat(token).isNotBlank();
    }

    @Test
    void validateRefreshToken_shouldReturnFalse_whenCacheIsNull() {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("refreshTokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        boolean valid = service.validateRefreshToken("token", "user");
        assertThat(valid).isFalse();
    }

    @Test
    void removeRefreshToken_shouldNotThrow_whenCacheIsNull() {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("refreshTokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        service.removeRefreshToken("token");
        // No exception means success
    }
    private JwtService jwtService;
    private CacheManager cacheManager;
    private Cache cache;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache("refreshTokens")).thenReturn(cache);
        jwtService = new JwtService(cacheManager);
    }

    @Test
    void generateRefreshToken_shouldStoreTokenInCache() {
        String username = "admin@email.com";
        String refreshToken = jwtService.generateRefreshToken(username);
        verify(cache).put(refreshToken, username);
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void validateRefreshToken_shouldReturnTrueIfTokenMatches() {
        String username = "admin@email.com";
        String refreshToken = "s3cr3t";
        when(cache.get(refreshToken, String.class)).thenReturn(username);
        boolean valid = jwtService.validateRefreshToken(refreshToken, username);
        assertThat(valid).isTrue();
    }

    @Test
    void validateRefreshToken_shouldReturnFalseIfTokenNotFound() {
        String username = "admin@email.com";
        String refreshToken = "s3cr3t";
        when(cache.get(refreshToken, String.class)).thenReturn(null);
        boolean valid = jwtService.validateRefreshToken(refreshToken, username);
        assertThat(valid).isFalse();
    }

    @Test
    void removeRefreshToken_shouldEvictFromCache() {
        String refreshToken = "s3cr3t";
        jwtService.removeRefreshToken(refreshToken);
        verify(cache).evict(refreshToken);
    }
}
