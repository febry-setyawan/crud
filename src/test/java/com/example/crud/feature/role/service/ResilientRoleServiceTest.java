package com.example.crud.feature.role.service;

import com.example.crud.feature.role.dto.RoleResponseDto;
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

@SpringBootTest(properties = "features.resilience.role.enabled=true")
class ResilientRoleServiceTest {

    @Autowired
    private RoleService resilientRoleService; // Spring injects the @Primary bean

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @SuppressWarnings("removal")
    @MockBean(name = "defaultRoleService") // Mock the underlying implementation
    private RoleService defaultRoleService;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        // Get and reset the specific circuit breaker for roles
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("roleService");
        circuitBreaker.reset();
    }

    @Test
    void createRole_shouldDelegateToDefaultService() {
        var requestDto = new com.example.crud.feature.role.dto.RoleRequestDto("USER", "User role");
        var expectedDto = new RoleResponseDto(2L, "USER", "User role");
        when(defaultRoleService.createRole(any())).thenReturn(expectedDto);

        RoleResponseDto result = resilientRoleService.createRole(requestDto);
        assertThat(result.name()).isEqualTo("USER");
        verify(defaultRoleService).createRole(any());
    }

    @Test
    void updateRole_shouldDelegateToDefaultService() {
        var requestDto = new com.example.crud.feature.role.dto.RoleRequestDto("USER", "User role");
        var expectedDto = new RoleResponseDto(3L, "USER", "User role");
        when(defaultRoleService.updateRole(eq(3L), any())).thenReturn(expectedDto);

        RoleResponseDto result = resilientRoleService.updateRole(3L, requestDto);
        assertThat(result.name()).isEqualTo("USER");
        verify(defaultRoleService).updateRole(eq(3L), any());
    }

    @Test
    void deleteRole_shouldDelegateToDefaultService() {
        when(defaultRoleService.deleteRole(4L)).thenReturn(true);
        boolean result = resilientRoleService.deleteRole(4L);
        assertThat(result).isTrue();
        verify(defaultRoleService).deleteRole(4L);
    }

    @Test
    void getAllRoles_whenDelegateSucceeds_shouldReturnData() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        var roleDto = new RoleResponseDto(5L, "USER", "User role");
        java.util.List<RoleResponseDto> roles = java.util.Collections.singletonList(roleDto);
        org.springframework.data.domain.Page<RoleResponseDto> expectedPage = new org.springframework.data.domain.PageImpl<>(
                roles, pageable, 1);
        when(defaultRoleService.getAllRoles(eq(pageable), any())).thenReturn(expectedPage);

        org.springframework.data.domain.Page<RoleResponseDto> result = resilientRoleService.getAllRoles(pageable, null);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("USER");
        verify(defaultRoleService).getAllRoles(eq(pageable), any());
    }

    @Test
    void getAllRoles_whenDelegateFails_shouldOpenCircuitAndFallback() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        when(defaultRoleService.getAllRoles(eq(pageable), any())).thenThrow(new RuntimeException("DB down"));

        // Open the circuit breaker by failing multiple times
        for (int i = 0; i < 10; i++) {
            try {
                resilientRoleService.getAllRoles(pageable, null);
            } catch (Exception ignored) {
                // Exception is intentionally ignored to simulate repeated failures and open the circuit breaker
            }
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Now call again, should hit fallback
        org.springframework.data.domain.Page<RoleResponseDto> fallbackPage = resilientRoleService.getAllRoles(pageable,
                null);
        assertThat(fallbackPage.getTotalElements()).isZero();
        assertThat(fallbackPage.getContent()).isEmpty();
        verify(defaultRoleService, times(10)).getAllRoles(eq(pageable), any());
    }

    @Test
    void getRoleById_whenDelegateSucceeds_shouldReturnData() {
        // Arrange
        RoleResponseDto expectedDto = new RoleResponseDto(1L, "ADMIN", "Admin role");
        when(defaultRoleService.getRoleById(1L)).thenReturn(expectedDto);

        // Act
        RoleResponseDto result = resilientRoleService.getRoleById(1L);

        // Assert
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(result.name()).isEqualTo("ADMIN");
        verify(defaultRoleService).getRoleById(1L);
    }

    @Test
    void getRoleById_whenDelegateFails_shouldOpenCircuitAndFallback() {
        // Arrange: Simulate the delegate service is always failing
        when(defaultRoleService.getRoleById(anyLong())).thenThrow(new RuntimeException("Database down!"));

        // Act & Assert: Call it multiple times to trip the circuit breaker
        for (int i = 0; i < 10; i++) {
            try {
                resilientRoleService.getRoleById(1L);
            } catch (Exception e) {
                // Ignore exceptions for this test's purpose
            }
        }

        // Verify the circuit is now OPEN
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Act: Call it one more time while the circuit is open
        RoleResponseDto fallbackResult = resilientRoleService.getRoleById(2L);

        // Assert: Check that the fallback method was called
        assertThat(fallbackResult.name()).isEqualTo("Fallback Role");

        // Verify that the delegate was NOT called again because the circuit was open
        verify(defaultRoleService, times(10)).getRoleById(anyLong());
    }
}