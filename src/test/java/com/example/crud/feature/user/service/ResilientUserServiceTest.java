package com.example.crud.feature.user.service;

import com.example.crud.feature.role.dto.RoleResponseDto; // <-- Import Role DTO
import com.example.crud.feature.user.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class ResilientUserServiceTest {

    @Autowired
    private UserService resilientUserService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @SuppressWarnings("removal")
    @MockBean(name = "defaultUserService")
    private UserService defaultUserService;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        circuitBreaker.reset();
    }

    @Test
    void getUserById_whenDelegateSucceeds_shouldReturnData() {
        // Arrange
        // Buat objek Role DTO untuk disertakan dalam User DTO
        RoleResponseDto roleDto = new RoleResponseDto(1L, "ADMIN", "Administrator");
        UserResponseDto expectedDto = new UserResponseDto(1L, "Test User", "test@example.com", roleDto);
        when(defaultUserService.getUserById(1L)).thenReturn(expectedDto);

        // Act
        UserResponseDto result = resilientUserService.getUserById(1L);

        // Assert
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.role().name()).isEqualTo("ADMIN"); // Verifikasi role
        verify(defaultUserService).getUserById(1L);
    }

    @Test
    void getUserById_whenDelegateFails_shouldOpenCircuitAndFallback() {
        // Arrange: Simulasikan service dasar selalu gagal
        when(defaultUserService.getUserById(anyLong())).thenThrow(new RuntimeException("Database down!"));

        // Act & Assert: Panggil beberapa kali untuk membuka sirkuit
        for (int i = 0; i < 10; i++) {
            try {
                resilientUserService.getUserById(1L);
            } catch (Exception e) {
                // Abaikan exception untuk tujuan test
            }
        }
        
        // Verifikasi sirkuit sekarang TERBUKA
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Act: Panggil sekali lagi saat sirkuit sudah terbuka
        UserResponseDto fallbackResult = resilientUserService.getUserById(2L);
        
        // Assert: Pastikan metode fallback yang dipanggil
        assertThat(fallbackResult.name()).isEqualTo("Fallback User");

        // Verifikasi bahwa service dasar TIDAK dipanggil lagi
        verify(defaultUserService, times(10)).getUserById(anyLong());
    }
}