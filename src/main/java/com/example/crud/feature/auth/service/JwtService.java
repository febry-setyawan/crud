package com.example.crud.feature.auth.service;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    private final String jwtSecret = "your-256-bit-secret"; // Use env/config in production
    private final long jwtExpirationMs = 86400000; // 1 day
    private final long refreshExpirationMs = 7 * 86400000; // 7 days

    @Autowired
    private CacheManager cacheManager;

    private static final String REFRESH_TOKEN_CACHE = "refreshTokens";

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
        if (cache == null) return false;
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
