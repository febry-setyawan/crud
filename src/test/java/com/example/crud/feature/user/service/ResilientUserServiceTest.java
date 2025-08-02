package com.example.crud.feature.user.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.crud.feature.user.dto.UserResponseDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ResilientUserServiceTest {

    @Autowired
    private UserService resilientUserService; // Spring akan inject @Primary bean, yaitu ResilientUserService

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @SuppressWarnings("removal")
    @MockBean(name = "defaultUserService") // Mock implementasi dasarnya
    private UserService defaultUserService;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        // Reset circuit breaker sebelum setiap test
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        circuitBreaker.reset();
    }

    @Test
    void getUserById_whenDelegateSucceeds_shouldReturnData() {
        // Arrange
        UserResponseDto expectedDto = new UserResponseDto(1L, "Test User", "test@example.com");
        when(defaultUserService.getUserById(1L)).thenReturn(expectedDto);

        // Act
        UserResponseDto result = resilientUserService.getUserById(1L);

        // Assert
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(result.name()).isEqualTo("Test User");
        verify(defaultUserService).getUserById(1L);
    }

    @Test
    void getUserById_whenDelegateFails_shouldOpenCircuitAndFallback() {
        // Arrange: Simulasikan service dasar selalu gagal
        when(defaultUserService.getUserById(anyLong())).thenThrow(new RuntimeException("Database down!"));

        // Act & Assert: Panggil beberapa kali untuk membuka sirkuit (sesuai sliding-window-size di properties)
        for (int i = 0; i < 10; i++) {
            try {
                resilientUserService.getUserById(1L);
            } catch (Exception e) {
                // Abaikan exception untuk tujuan test ini
            }
        }
        
        // Verifikasi sirkuit sekarang TERBUKA
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Act: Panggil sekali lagi saat sirkuit sudah terbuka
        UserResponseDto fallbackResult = resilientUserService.getUserById(2L);
        
        // Assert: Pastikan metode fallback yang dipanggil
        assertThat(fallbackResult.name()).isEqualTo("Fallback User");

        // Verifikasi bahwa service dasar TIDAK dipanggil lagi karena sirkuit terbuka
        verify(defaultUserService, times(10)).getUserById(anyLong());
    }
}