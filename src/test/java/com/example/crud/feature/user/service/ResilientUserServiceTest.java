package com.example.crud.feature.user.service;

import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "features.resilience.user.enabled=true")
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
        void createUser_shouldDelegateToDefaultService() {
            RoleResponseDto roleDto = new RoleResponseDto(2L, "USER", "User role");
            UserResponseDto expectedDto = new UserResponseDto(2L, "Created User", "created@example.com", roleDto);
            when(defaultUserService.createUser(any())).thenReturn(expectedDto);

            UserResponseDto result = resilientUserService.createUser(null);
            assertThat(result.username()).isEqualTo("Created User");
            verify(defaultUserService).createUser(any());
        }

        @Test
        void updateUser_shouldDelegateToDefaultService() {
            RoleResponseDto roleDto = new RoleResponseDto(3L, "USER", "User role");
            UserResponseDto expectedDto = new UserResponseDto(3L, "Updated User", "updated@example.com", roleDto);
            when(defaultUserService.updateUser(eq(3L), any())).thenReturn(expectedDto);

            UserResponseDto result = resilientUserService.updateUser(3L, null);
            assertThat(result.username()).isEqualTo("Updated User");
            verify(defaultUserService).updateUser(eq(3L), any());
        }

        @Test
        void deleteUser_shouldDelegateToDefaultService() {
            when(defaultUserService.deleteUser(4L)).thenReturn(true);
            boolean result = resilientUserService.deleteUser(4L);
            assertThat(result).isTrue();
            verify(defaultUserService).deleteUser(4L);
        }

        @Test
        void getAllUsers_whenDelegateSucceeds_shouldReturnData() {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
            RoleResponseDto roleDto = new RoleResponseDto(5L, "USER", "User role");
            UserResponseDto userDto = new UserResponseDto(5L, "User5", "user5@example.com", roleDto);
            java.util.List<UserResponseDto> users = java.util.Collections.singletonList(userDto);
            org.springframework.data.domain.Page<UserResponseDto> expectedPage = new org.springframework.data.domain.PageImpl<>(users, pageable, 1);
            when(defaultUserService.getAllUsers(eq(pageable), any())).thenReturn(expectedPage);

            org.springframework.data.domain.Page<UserResponseDto> result = resilientUserService.getAllUsers(pageable, null);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).username()).isEqualTo("User5");
            verify(defaultUserService).getAllUsers(eq(pageable), any());
        }

        @Test
        void getAllUsers_whenDelegateFails_shouldOpenCircuitAndFallback() {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
            when(defaultUserService.getAllUsers(eq(pageable), any())).thenThrow(new RuntimeException("DB down"));

            // Open the circuit breaker by failing multiple times
            for (int i = 0; i < 10; i++) {
                try {
                    resilientUserService.getAllUsers(pageable, null);
                } catch (Exception ignored) {}
            }
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            // Now call again, should hit fallback
            org.springframework.data.domain.Page<UserResponseDto> fallbackPage = resilientUserService.getAllUsers(pageable, null);
            assertThat(fallbackPage.getTotalElements()).isEqualTo(0);
            assertThat(fallbackPage.getContent()).isEmpty();
            verify(defaultUserService, times(10)).getAllUsers(eq(pageable), any());
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
        assertThat(result.username()).isEqualTo("Test User");
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
        assertThat(fallbackResult.username()).isEqualTo("fallback@example.com");

        // Verifikasi bahwa service dasar TIDAK dipanggil lagi
        verify(defaultUserService, times(10)).getUserById(anyLong());
    }
}