package com.example.crud.feature.auth.controller;

import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshRequest;
import com.example.crud.feature.auth.dto.RefreshResponse;
import com.example.crud.feature.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public RefreshResponse login(@RequestBody AuthRequest authRequest) {
        log.info("Login attempt: username='{}', password='{}'", authRequest.getUsername(), authRequest.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtService.generateToken(authRequest.getUsername());
            String refreshToken = jwtService.generateRefreshToken(authRequest.getUsername());
            log.info("Login success for username='{}'", authRequest.getUsername());
            return new RefreshResponse(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("Login failed for username='{}': {}", authRequest.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        // Find username from refresh token in cache
        String refreshToken = request.getRefreshToken();
        String username = null;
        var cache = jwtService.getCacheManager().getCache("refreshTokens");
        if (cache != null) {
            username = cache.get(refreshToken, String.class);
        }
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        // Optionally: remove old refresh token and issue a new one (rotation)
        jwtService.removeRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest request) {
        jwtService.removeRefreshToken(request.getRefreshToken());
    }
}
