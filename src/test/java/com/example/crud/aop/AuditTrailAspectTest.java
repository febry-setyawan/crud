
package com.example.crud.aop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuditTrailAspectTest {
    @Autowired
    private AuditTrailAspect auditTrailAspect;

    @Autowired
    private AuditableTestEntityRepository auditableTestEntityRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Create a simple table for testing
        jdbcTemplate.execute("CREATE TABLE test_auditable_entity (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(255), " +
                "created_at TIMESTAMP, " +
                "created_by VARCHAR(255), " +
                "updated_at TIMESTAMP, " +
                "updated_by VARCHAR(255)" +
                ")");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DROP TABLE test_auditable_entity");
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username = "testuser")
    void beforeSave_shouldSetAuditFields_whenUserIsAuthenticated() {
        // Given
        AuditableTestEntity entity = new AuditableTestEntity(null, "New Entity");

        // When
        AuditableTestEntity savedEntity = auditableTestEntityRepository.save(entity);

        // Then
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getCreatedBy()).isEqualTo("testuser");
        assertThat(savedEntity.getUpdatedAt()).isEqualTo(savedEntity.getCreatedAt());
        assertThat(savedEntity.getUpdatedBy()).isEqualTo("testuser");
    }

    @Test
    void beforeSave_shouldSetSystemAsUser_whenUserIsAnonymous() {
        // Given
        SecurityContextHolder.clearContext();
        AuditableTestEntity entity = new AuditableTestEntity(null, "New Entity");

        // When
        AuditableTestEntity savedEntity = auditableTestEntityRepository.save(entity);

        // Then
        assertThat(savedEntity.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(savedEntity.getUpdatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    void beforeUpdate_shouldUpdateAuditFields() {

        // Given
        AuditableTestEntity entity = auditableTestEntityRepository.save(new AuditableTestEntity(0L, "Initial Name"));
        LocalDateTime initialUpdatedAt = entity.getUpdatedAt();

        // When
        AuditableTestEntity toUpdate = auditableTestEntityRepository.findById(entity.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        auditableTestEntityRepository.update(toUpdate);

        // Reload entity from repository
        AuditableTestEntity reloaded = auditableTestEntityRepository.findById(entity.getId()).orElseThrow();

        // Then
        assertThat(reloaded.getUpdatedAt()).isAfter(initialUpdatedAt);
        assertThat(reloaded.getUpdatedBy()).isNotNull();
    }

    @Test
    @WithMockUser(username = "update_user")
    void beforeUpdate_shouldUpdateAuditFields_withMockUser() {
        // Given
        AuditableTestEntity entity = auditableTestEntityRepository.save(new AuditableTestEntity(0L, "Initial Name"));
        LocalDateTime initialUpdatedAt = entity.getUpdatedAt();

        // When
        // Wait until the system clock moves forward to ensure updatedAt will be different
        LocalDateTime now = LocalDateTime.now();
        while (!now.isAfter(initialUpdatedAt)) {
            now = LocalDateTime.now();
        }
        AuditableTestEntity toUpdate = auditableTestEntityRepository.findById(entity.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        auditableTestEntityRepository.update(toUpdate);

        // Reload entity from repository
        AuditableTestEntity reloaded = auditableTestEntityRepository.findById(entity.getId()).orElseThrow();

        // Then
        assertThat(reloaded.getUpdatedAt()).isAfter(initialUpdatedAt);
        assertThat(reloaded.getUpdatedBy()).isEqualTo("update_user");
    }

    @Test
    @DisplayName("getCurrentUsername returns SYSTEM if isAuthenticated is false")
    void getCurrentUsername_shouldReturnSystem_whenNotAuthenticated() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String username = auditTrailAspect.getClass().getDeclaredMethod("getCurrentUsername").trySetAccessible() ?
            (String) auditTrailAspect.getClass().getDeclaredMethod("getCurrentUsername").invoke(auditTrailAspect) : null;
        assertThat(username).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("getCurrentUsername returns SYSTEM if getName is anonymousUser")
    void getCurrentUsername_shouldReturnSystem_whenAnonymousUser() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);
        String username = auditTrailAspect.getClass().getDeclaredMethod("getCurrentUsername").trySetAccessible() ?
            (String) auditTrailAspect.getClass().getDeclaredMethod("getCurrentUsername").invoke(auditTrailAspect) : null;
        assertThat(username).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("aroundUpdate should propagate exception from pjp.proceed()")
    void aroundUpdate_shouldPropagateException() throws Throwable {
        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        Object entity = Mockito.mock(com.example.crud.common.model.Auditable.class);
        Mockito.when(pjp.proceed()).thenThrow(new RuntimeException("proceed error"));
        java.lang.reflect.Method method = auditTrailAspect.getClass().getDeclaredMethod("aroundUpdate", ProceedingJoinPoint.class, Object.class);
        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            method.invoke(auditTrailAspect, pjp, entity);
        });
        assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getCause().getMessage()).isEqualTo("proceed error");
    }
}
