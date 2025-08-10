
package com.example.crud.feature.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.token.refresh.expiration}")
    private long jwtRefreshExpirationMs;

    @Value("${cache.tokens.name:tokens}")
    private String refreshTokenCacheName;

    private final CacheManager cacheManager;

    public JwtService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public String getRefreshTokenCacheName() {
        return refreshTokenCacheName;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        Cache cache = cacheManager.getCache(refreshTokenCacheName);
        if (cache != null) {
            cache.put(refreshToken, username);
        }
        return refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken, String username) {
        Cache cache = cacheManager.getCache(refreshTokenCacheName);
        if (cache == null)
            return false;
        String cachedUsername = cache.get(refreshToken, String.class);
        return username.equals(cachedUsername);
    }

    public void removeRefreshToken(String refreshToken) {
        Cache cache = cacheManager.getCache(refreshTokenCacheName);
        if (cache != null) {
            cache.evict(refreshToken);
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
