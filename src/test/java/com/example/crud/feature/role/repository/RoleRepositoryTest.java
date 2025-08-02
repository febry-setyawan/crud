package com.example.crud.feature.role.repository;

import com.example.crud.feature.role.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(RoleRepositoryTest.TestRepoConfiguration.class)
@Sql("/db/migration/V2__Create_roles_table.sql")
class RoleRepositoryTest {

    @TestConfiguration
    // Konfigurasi ini hanya mengimpor RoleRepository, tanpa Aspect audit
    @Import(RoleRepository.class)
    static class TestRepoConfiguration {}

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUpDatabase() {
        // Simpan beberapa data awal untuk testing
        roleRepository.save(new Role("USER", "Standard user role"));
        roleRepository.save(new Role("ADMIN", "Administrator role"));
        roleRepository.save(new Role("GUEST", "Guest role"));
    }

    @Test
    void save_shouldInsertRoleAndReturnWithId() {
        // Arrange
        Role newRole = new Role("SUPPORT", "Support team role");

        // Act
        Role savedRole = roleRepository.save(newRole);

        // Assert
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull().isPositive();
        assertThat(savedRole.getName()).isEqualTo("SUPPORT");
    }

    @Test
    void findById_whenRoleExists_shouldReturnRole() {
        // Act
        Optional<Role> foundRole = roleRepository.findById(1L); // Asumsikan ID 1 adalah USER

        // Assert
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("USER");
    }

    @Test
    void findAll_withPagination_shouldReturnPaginatedResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Role> result = roleRepository.findAll(pageable, Map.of());

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAll_withSort_shouldReturnSortedPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name").ascending());

        // Act
        Page<Role> result = roleRepository.findAll(pageable, Map.of());

        // Assert
        assertThat(result.getContent())
                .extracting(Role::getName)
                .containsExactly("ADMIN", "GUEST", "USER");
    }

    @Test
    void update_shouldModifyExistingRole() {
        // Arrange
        Role roleToUpdate = roleRepository.findById(1L).orElseThrow();
        roleToUpdate.setDescription("Updated description");

        // Act
        int updatedRows = roleRepository.update(roleToUpdate);
        Optional<Role> updatedRole = roleRepository.findById(1L);

        // Assert
        assertThat(updatedRows).isEqualTo(1);
        assertThat(updatedRole).isPresent();
        assertThat(updatedRole.get().getDescription()).isEqualTo("Updated description");
    }
}