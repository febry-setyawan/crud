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

    private static final String REFRESH_TOKEN_CACHE = "refreshTokens";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    private final CacheManager cacheManager;

    public JwtService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
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
        Cache cache = cacheManager.getCache(REFRESH_TOKEN_CACHE);
        if (cache != null) {
            cache.put(refreshToken, username);
        }
        return refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken, String username) {
        Cache cache = cacheManager.getCache(REFRESH_TOKEN_CACHE);
        if (cache == null)
            return false;
        String cachedUsername = cache.get(refreshToken, String.class);
        return username.equals(cachedUsername);
    }

    public void removeRefreshToken(String refreshToken) {
        Cache cache = cacheManager.getCache(REFRESH_TOKEN_CACHE);
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
