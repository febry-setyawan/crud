
package com.example.crud.feature.auth.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.crud.feature.auth.exception.InvalidRefreshTokenException;
import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshResponse;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RefreshResponse login(AuthRequest authRequest) {
        logger.debug("Authenticating username={}", authRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(),
                authRequest.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtService.generateToken(authRequest.getUsername());
        String refreshToken = jwtService.generateRefreshToken(authRequest.getUsername());
        logger.info("Login successful for username={}", authRequest.getUsername());
        return new RefreshResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(String refreshToken) {
        String username = jwtService.getCacheManager().getCache("refreshTokens").get(refreshToken, String.class);
        if (username == null) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        logger.info("Refreshing token for username={}", username);
        jwtService.removeRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        jwtService.removeRefreshToken(refreshToken);
        logger.info("User logged out and refresh token invalidated");
    }
}

