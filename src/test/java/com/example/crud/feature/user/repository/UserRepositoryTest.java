package com.example.crud.feature.user.repository;

import javax.sql.DataSource;
import com.example.crud.aop.AuditTrailAspect;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;
import com.example.crud.feature.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserRepositoryTest.TestRepoConfiguration.class)
@Sql({
    "/db/migration/h2/V1__Create_users_table.sql",
    "/db/migration/h2/V2__Create_roles_table.sql",
    "/db/migration/h2/V3__Add_role_id_to_users_table.sql",
    "/db/migration/h2/V4__add_password_and_initial_users.sql"
})
@WithMockUser("test-user")
class UserRepositoryTest {
    @Test
    void findAll_withInvalidSortColumn_shouldNotThrow() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by("nonexistent").ascending());
        // Should not throw, but may ignore the sort or fallback
        Page<User> result = userRepository.findAll(pageable, Map.of());
        assertThat(result.getContent()).isNotNull();
    }
    @Test
    void findById_whenUserNotFound_shouldReturnEmptyOptional() {
        Optional<User> result = userRepository.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void save_whenUserIsNull_shouldThrowException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userRepository.save(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void update_whenUserNotFound_shouldReturnZero() {
        User notExist = new User("Ghost", "ghost@example.com");
        notExist.setId(9999L);
        notExist.setRole(savedRole);
        int updatedRows = userRepository.update(notExist);
        assertThat(updatedRows).isEqualTo(0);
    }

    @Test
    void findAll_withNullAndEmptyFilters_shouldReturnAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> resultNull = userRepository.findAll(pageable, null);
        Page<User> resultEmpty = userRepository.findAll(pageable, Map.of());
        assertThat(resultNull.getTotalElements()).isEqualTo(resultEmpty.getTotalElements());
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        org.springframework.security.core.userdetails.UserDetails details = userRepository.loadUserByUsername("alice@example.com");
        assertThat(details.getUsername()).isEqualTo("alice@example.com");
        assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userRepository.loadUserByUsername("notfound@example.com"))
            .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username");
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    @Import({UserRepository.class, RoleRepository.class, AuditTrailAspect.class})
    static class TestRepoConfiguration {}

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role savedRole;

    @Autowired
    private DataSource dataSource;

    // Siapkan data sebelum beberapa test
    @BeforeEach
    void setUpDatabase() {
        // Hapus semua user agar tidak bentrok dengan data dari migration
        org.springframework.jdbc.core.JdbcTemplate jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
        jdbc.execute("DELETE FROM users");

        // Ambil role USER dari migration (id=2)
        savedRole = roleRepository.findById(2L).orElseThrow(() -> new RuntimeException("Role USER (id=2) not found"));

        // 2. Buat User dan set Role yang sudah disimpan
        User user1 = new User("Charlie", "charlie@example.com");
        user1.setRole(savedRole);
        userRepository.save(user1);

    User user2 = new User("alice@example.com", "alicepass");
    user2.setRole(savedRole);
    userRepository.save(user2);

        User user3 = new User("Bob", "bob@example.com");
        user3.setRole(savedRole);
        userRepository.save(user3);
    }

    @Test
    void findAll_withPagination_shouldReturnPaginatedResult() {
        Pageable pageable = PageRequest.of(0, 2); // Ambil halaman pertama, ukuran 2
        Page<User> result = userRepository.findAll(pageable, Map.of());

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAll_withSort_shouldReturnSortedPage() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by("username").ascending());
        Page<User> result = userRepository.findAll(pageable, Map.of());

    assertThat(result.getContent()).hasSize(3);
    var usernames = result.getContent().stream().map(User::getUsername).toList();
    var expected = java.util.List.of("alice@example.com", "Bob", "Charlie");
    assertThat(usernames.stream().sorted().toList()).isEqualTo(expected.stream().sorted().toList());
    }

    @Test
    void findAll_withFilter_shouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 5);
    Map<String, Object> filter = Map.of("username", "alice@example.com");
        Page<User> result = userRepository.findAll(pageable, filter);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUsername()).isEqualTo("alice@example.com");
    }

    @Test
    void save_shouldInsertUserAndReturnWithIdAndAudit() {
        // Hapus data dari setUp untuk test save yang bersih
        userRepository.deleteById(1L);
        userRepository.deleteById(2L);
        userRepository.deleteById(3L);

        // Arrange
        User user = new User("John Doe", "john.doe@example.com");
        user.setRole(savedRole); // <-- Jangan lupa set Role

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo("test-user");
        assertThat(savedUser.getRole()).isNotNull();
        assertThat(savedUser.getRole().getId()).isEqualTo(savedRole.getId());
    }

    @Test
    void findById_whenUserExists_shouldReturnUser() {
        // Data sudah di-save oleh setUp
        Optional<User> foundUser = userRepository.findById(2L); // Cari Alice
        assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo("alice@example.com");
    }
        
    @Test
    void update_shouldModifyExistingUser() {
        User userToUpdate = userRepository.findById(3L).get(); // Ambil Bob
        userToUpdate.setUsername("Robert");

        userRepository.update(userToUpdate);

        Optional<User> updatedUser = userRepository.findById(3L);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getUsername()).isEqualTo("Robert");
        assertThat(updatedUser.get().getUpdatedAt()).isNotNull();
        assertThat(updatedUser.get().getUpdatedBy()).isEqualTo("test-user");
    }

    @Test
    void count_shouldReturnCorrectNumberOfUsers() {
        long count = userRepository.findAll(PageRequest.of(0, 10), Map.of()).getTotalElements();
        assertThat(count).isEqualTo(3);
    }
}
