package com.example.crud.feature.auth.service;

import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("password");
        // Stub getRefreshTokenCacheName agar tidak null pada mock JwtService
        lenient().when(jwtService.getRefreshTokenCacheName()).thenReturn("tokens");
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("user")).thenReturn("accessToken");
        when(jwtService.generateRefreshToken("user")).thenReturn("refreshToken");

        RefreshResponse response = authenticationService.login(authRequest);

        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(authRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        when(jwtService.getCacheManager()).thenReturn(cacheManager);
    when(cacheManager.getCache("tokens")).thenReturn(cache);
        when(cache.get("validToken", String.class)).thenReturn("user");
        when(jwtService.generateToken("user")).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken("user")).thenReturn("newRefreshToken");

        RefreshResponse response = authenticationService.refresh("validToken");

        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        verify(jwtService).removeRefreshToken("validToken");
    }

    @Test
    void refresh_shouldThrowException_whenRefreshTokenIsInvalid() {
        when(jwtService.getCacheManager()).thenReturn(cacheManager);
    when(cacheManager.getCache("tokens")).thenReturn(cache);
        when(cache.get("invalidToken", String.class)).thenReturn(null);

        assertThatThrownBy(() -> authenticationService.refresh("invalidToken"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void logout_shouldRemoveRefreshToken() {
        authenticationService.logout("someToken");
        verify(jwtService).removeRefreshToken("someToken");
    }
}
