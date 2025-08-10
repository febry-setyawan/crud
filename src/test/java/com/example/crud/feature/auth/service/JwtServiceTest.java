package com.example.crud.feature.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

class JwtServiceTest {
    private JwtService jwtService;
    private CacheManager cacheManager;
    private Cache cache;

    @BeforeEach
    void setUp() throws Exception {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache("tokens")).thenReturn(cache);
        jwtService = new JwtService(cacheManager);
        // Set refreshTokenCacheName agar tidak null
        java.lang.reflect.Field field = JwtService.class.getDeclaredField("refreshTokenCacheName");
        field.setAccessible(true);
        field.set(jwtService, "tokens");
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

        java.lang.reflect.Field cacheNameField = JwtService.class.getDeclaredField("refreshTokenCacheName");
        cacheNameField.setAccessible(true);
        cacheNameField.set(service, "tokens");

        // Act
        String token = service.generateToken(username);
        String extracted = service.getUsernameFromToken(token);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(extracted).isEqualTo(username);
    }

    @Test
    void generateRefreshToken_shouldNotThrow_whenCacheIsNull() throws Exception {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("tokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        java.lang.reflect.Field cacheNameField = JwtService.class.getDeclaredField("refreshTokenCacheName");
        cacheNameField.setAccessible(true);
        cacheNameField.set(service, "tokens");
        String token = service.generateRefreshToken("user");
        assertThat(token).isNotBlank();
    }

    @Test
    void validateRefreshToken_shouldReturnFalse_whenCacheIsNull() throws Exception {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("tokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        java.lang.reflect.Field cacheNameField = JwtService.class.getDeclaredField("refreshTokenCacheName");
        cacheNameField.setAccessible(true);
        cacheNameField.set(service, "tokens");
        boolean valid = service.validateRefreshToken("s3cr3t", "user");
        assertThat(valid).isFalse();
    }

    @Test
    void removeRefreshToken_shouldNotThrow_whenCacheIsNull() throws Exception {
        CacheManager nullCacheManager = mock(CacheManager.class);
        when(nullCacheManager.getCache("tokens")).thenReturn(null);
        JwtService service = new JwtService(nullCacheManager);
        java.lang.reflect.Field cacheNameField = JwtService.class.getDeclaredField("refreshTokenCacheName");
        cacheNameField.setAccessible(true);
        cacheNameField.set(service, "tokens");
        service.removeRefreshToken("s3cr3t");
        // No exception means success
        assertThatCode(() -> service.removeRefreshToken("s3cr3t")).doesNotThrowAnyException();
    }

    @Test
    void generateToken_withRoles_shouldContainRolesClaim() throws Exception {
        String username = "testuser";
        String secret = "mySecretKey1234567890";
        long expiration = 3600000L;
        JwtService service = new JwtService(mock(CacheManager.class));

        // Set private fields
        Field secretField = JwtService.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(service, secret);

        Field expField = JwtService.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(service, expiration);

        Field cacheNameField = JwtService.class.getDeclaredField("refreshTokenCacheName");
        cacheNameField.setAccessible(true);
        cacheNameField.set(service, "tokens");

        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        String token = service.generateToken(username, roles);

        // Parse token and check claims
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        assertThat(claims.getSubject()).isEqualTo(username);
        @SuppressWarnings("unchecked")
        List<String> rolesClaim = (List<String>) claims.get("roles", List.class);
        assertThat(rolesClaim).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }
}
