package com.example.crud.aop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuditTrailAspectTest {

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
    void beforeSave_shouldSetAuditFields_whenUserIsAuthenticated() {
        // Given
        String username = "testuser";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null)
        );

        AuditableTestEntity entity = new AuditableTestEntity();
        entity.setName("New Entity");

        // When
        AuditableTestEntity savedEntity = auditableTestEntityRepository.save(entity);

        // Then
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getCreatedBy()).isEqualTo(username);
        assertThat(savedEntity.getUpdatedAt()).isEqualTo(savedEntity.getCreatedAt());
        assertThat(savedEntity.getUpdatedBy()).isEqualTo(username);
    }

    @Test
    void beforeSave_shouldSetSystemAsUser_whenUserIsAnonymous() {
        // Given
        AuditableTestEntity entity = new AuditableTestEntity();
        entity.setName("New Entity");

        // When
        AuditableTestEntity savedEntity = auditableTestEntityRepository.save(entity);

        // Then
        assertThat(savedEntity.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(savedEntity.getUpdatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    void beforeUpdate_shouldUpdateAuditFields() throws InterruptedException {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("initial_user", null)
        );
        AuditableTestEntity entity = auditableTestEntityRepository.save(new AuditableTestEntity(0L,"Initial Name"));

        LocalDateTime initialUpdatedAt = entity.getUpdatedAt();

        // When
        Thread.sleep(10); // Ensure timestamp changes
        String updateUser = "update_user";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(updateUser, null)
        );
        entity.setName("Updated Name");
        auditableTestEntityRepository.update(entity);

        // Then
        assertThat(entity.getUpdatedAt()).isAfter(initialUpdatedAt);
        assertThat(entity.getUpdatedBy()).isEqualTo(updateUser);
    }
