
package com.example.crud.feature.auth.filter;
import org.junit.jupiter.api.AfterEach;

import com.example.crud.feature.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Ensure clean context
        SecurityContextHolder.clearContext();
        // Given
        when(request.getHeader("Authorization")).thenReturn("Token somethingelse");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication should remain null (not set by filter)
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    @Test
    void doFilterInternal_shouldNotAuthenticate_whenUserDetailsIsNull() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String username = "user";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String username = "user";
        UserDetails userDetails = new User(username, "password", new ArrayList<>());

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid() throws ServletException, IOException {
        // Given
        String token = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getUsernameFromToken(token)).thenThrow(new JwtException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenNoTokenIsProvided() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenUserIsNotFound() throws ServletException, IOException {

    // Given
    String token = "validToken";
    String username = "unknownUser";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.getUsernameFromToken(token)).thenReturn(username);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    // Authentication harus null atau principal-nya null jika user tidak ditemukan
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication == null || authentication.getPrincipal() == null).isTrue();
    verify(filterChain).doFilter(request, response);
    }
}
