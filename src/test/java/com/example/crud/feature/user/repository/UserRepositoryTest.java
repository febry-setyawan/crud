package com.example.crud.feature.user.repository;

import com.example.crud.aop.AuditTrailAspect;
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
@Sql("/db/migration/V1__Create_users_table.sql")
@WithMockUser("test-user")
class UserRepositoryTest {

    @TestConfiguration
    @EnableAspectJAutoProxy
    @Import({UserRepository.class, AuditTrailAspect.class})
    static class TestRepoConfiguration {}

    @Autowired
    private UserRepository userRepository;

    // Siapkan data sebelum beberapa test
    @BeforeEach
    void setUpDatabase() {
        userRepository.save(new User("Charlie", "charlie@example.com"));
        userRepository.save(new User("Alice", "alice@example.com"));
        userRepository.save(new User("Bob", "bob@example.com"));
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
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name").ascending());
        Page<User> result = userRepository.findAll(pageable, Map.of());

        assertThat(result.getContent()).hasSize(3)
                .extracting(User::getName)
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void findAll_withFilter_shouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Map<String, Object> filter = Map.of("name", "Alice");
        Page<User> result = userRepository.findAll(pageable, filter);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void save_shouldInsertUserAndReturnWithIdAndAudit() {
        // Hapus data dari setUp untuk test save yang bersih
        userRepository.deleteById(1L);
        userRepository.deleteById(2L);
        userRepository.deleteById(3L);

        User user = new User("John Doe", "john.doe@example.com");
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo("test-user");
    }

    @Test
    void findById_whenUserExists_shouldReturnUser() {
        // Data sudah di-save oleh setUp
        Optional<User> foundUser = userRepository.findById(2L); // Cari Alice
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Alice");
    }
        
    @Test
    void update_shouldModifyExistingUser() {
        User userToUpdate = userRepository.findById(3L).get(); // Ambil Bob
        userToUpdate.setName("Robert");
        
        userRepository.update(userToUpdate);

        Optional<User> updatedUser = userRepository.findById(3L);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getName()).isEqualTo("Robert");
        assertThat(updatedUser.get().getUpdatedAt()).isNotNull();
        assertThat(updatedUser.get().getUpdatedBy()).isEqualTo("test-user");
    }
}