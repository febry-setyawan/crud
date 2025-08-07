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