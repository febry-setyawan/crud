package com.example.crud.feature.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtServiceTest {
    @Test
    void generateToken_and_getUsernameFromToken_shouldWork() throws Exception {
        // Arrange
        String username = "testuser";
        String secret = "mySecretKey1234567890";
        long expiration = 3600000L;
        JwtService service = new JwtService(mock(CacheManager.class));

        // Use reflection to set private fields
        java.lang.reflect.Field secretField = JwtService.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(service, secret);

        java.lang.reflect.Field expField = JwtService.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(service, expiration);

        // Act
        String token = service.generateToken(username);
        String extracted = service.getUsernameFromToken(token);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(extracted).isEqualTo(username);
    }
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
        assertThat(true).isTrue();
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
