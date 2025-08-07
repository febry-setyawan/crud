package com.example.crud.feature.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshResponse;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public RefreshResponse login(AuthRequest authRequest) {
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
            return new RefreshResponse(accessToken, refreshToken);
        } catch (Exception e) {
            throw e;
        }
    }   

    public RefreshResponse refresh(String refreshToken) {
        String username = jwtService.getCacheManager().getCache("refreshTokens").get(refreshToken, String.class);
        if (username == null) {
            throw new RuntimeException("Invalid refresh token");
        }
        jwtService.removeRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        jwtService.removeRefreshToken(refreshToken);
    }
}
