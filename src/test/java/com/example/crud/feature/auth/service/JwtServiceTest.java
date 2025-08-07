package com.example.crud.feature.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JwtServiceTest {
    private JwtService jwtService;
    private CacheManager cacheManager;
    private Cache cache;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache("refreshTokens")).thenReturn(cache);
        jwtService = new JwtService();
        // Inject mock cacheManager
        org.springframework.test.util.ReflectionTestUtils.setField(jwtService, "cacheManager", cacheManager);
    }

    @Test
    void generateRefreshToken_shouldStoreTokenInCache() {
        String username = "user1";
        String refreshToken = jwtService.generateRefreshToken(username);
        verify(cache).put(refreshToken, username);
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void validateRefreshToken_shouldReturnTrueIfTokenMatches() {
        String username = "user1";
        String refreshToken = "token123";
        when(cache.get(refreshToken, String.class)).thenReturn(username);
        boolean valid = jwtService.validateRefreshToken(refreshToken, username);
        assertThat(valid).isTrue();
    }

    @Test
    void validateRefreshToken_shouldReturnFalseIfTokenNotFound() {
        String username = "user1";
        String refreshToken = "token123";
        when(cache.get(refreshToken, String.class)).thenReturn(null);
        boolean valid = jwtService.validateRefreshToken(refreshToken, username);
        assertThat(valid).isFalse();
    }

    @Test
    void removeRefreshToken_shouldEvictFromCache() {
        String refreshToken = "token123";
        jwtService.removeRefreshToken(refreshToken);
        verify(cache).evict(refreshToken);
    }
}
