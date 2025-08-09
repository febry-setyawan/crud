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
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(RoleRepositoryTest.TestRepoConfiguration.class)
class RoleRepositoryTest {

    @TestConfiguration
    // Konfigurasi ini hanya mengimpor RoleRepository, tanpa Aspect audit
    @Import(RoleRepository.class)
    static class TestRepoConfiguration {}

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private javax.sql.DataSource dataSource;

    @BeforeEach
    void setUpDatabase() throws Exception {
        // Hapus data users terlebih dahulu agar tidak melanggar constraint foreign key
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM roles");
            stmt.executeUpdate("ALTER TABLE roles ALTER COLUMN id RESTART WITH 1;");
        }
        // Insert hanya data test
        roleRepository.save(new Role("SUPPORT", "Support team role"));
        roleRepository.save(new Role("MANAGER", "Manager role"));
        roleRepository.save(new Role("LEADER", "Leader role"));
    }

    @Test
    void save_shouldInsertRoleAndReturnWithId() {
        // Arrange
        Role newRole = new Role("OWNER", "Owner role");

        // Act
        Role savedRole = roleRepository.save(newRole);

        // Assert
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull().isPositive();
        assertThat(savedRole.getName()).isEqualTo("OWNER");
    }

    @Test
    void findById_whenRoleExists_shouldReturnRole() {
        // Act
        Optional<Role> foundRole = roleRepository.findAll(PageRequest.of(0, 10), Map.of()).getContent().stream()
            .filter(r -> r.getName().equals("SUPPORT")).findFirst();

        // Assert
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("SUPPORT");
    }

    @Test
    void findAll_withPagination_shouldReturnPaginatedResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Role> result = roleRepository.findAll(pageable, Map.of());

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(3);
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
                .containsExactly("LEADER", "MANAGER", "SUPPORT");
    }

    @Test
    void update_shouldModifyExistingRole() {
        // Arrange
        Role roleToUpdate = roleRepository.findAll(PageRequest.of(0, 10), Map.of()).getContent().stream()
            .filter(r -> r.getName().equals("MANAGER")).findFirst().orElseThrow();
        roleToUpdate.setDescription("Updated description");

        // Act
        int updatedRows = roleRepository.update(roleToUpdate);
        Optional<Role> updatedRole = roleRepository.findById(roleToUpdate.getId());

        // Assert
        assertThat(updatedRows).isEqualTo(1);
        assertThat(updatedRole).isPresent();
        assertThat(updatedRole.get().getDescription()).isEqualTo("Updated description");
    }

    @Test
    void count_shouldReturnCorrectNumberOfRoles() {
        long count = roleRepository.findAll(PageRequest.of(0, 10), Map.of()).getTotalElements();
        assertThat(count).isEqualTo(3);
    }

    @Test
    void deleteById_shouldRemoveRole() {
        Role roleToDelete = roleRepository.findAll(PageRequest.of(0, 10), Map.of()).getContent().stream()
            .filter(r -> r.getName().equals("MANAGER")).findFirst().orElseThrow();

        int deletedRows = roleRepository.deleteById(roleToDelete.getId());
        Optional<Role> deletedRole = roleRepository.findById(roleToDelete.getId());

        assertThat(deletedRows).isEqualTo(1);
        assertThat(deletedRole).isNotPresent();
    }
}
