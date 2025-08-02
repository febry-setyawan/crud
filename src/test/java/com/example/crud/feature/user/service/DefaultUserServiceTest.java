package com.example.crud.feature.user.service;

import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;
import com.example.crud.feature.user.dto.UserFilterDto;
import com.example.crud.feature.user.dto.UserMapper;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import com.example.crud.feature.user.model.User;
import com.example.crud.feature.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository; // <-- Mock untuk RoleRepository

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    // Objek data untuk testing
    private User user;
    private Role role;
    private UserResponseDto userResponseDto;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @BeforeEach
    void setUp() {
        // Inisialisasi service dengan semua mock
        userService = new DefaultUserService(userRepository, roleRepository, userMapper);

        // Siapkan data Role dan User
        role = new Role("ADMIN", "Administrator");
        role.setId(1L);

        user = new User("Test User", "test@example.com");
        user.setId(1L);
        user.setRole(role);

        // Siapkan DTO response
        // Di dunia nyata, mapper akan menangani ini, tapi untuk mock kita definisikan manual
        userResponseDto = new UserResponseDto(1L, "Test User", "test@example.com", null);
    }

    @Test
    void getAllUsers_withFilter_shouldBuildMapWithWildcardsAndCallRepository() {
        // Arrange
        UserFilterDto filterDto = new UserFilterDto();
        filterDto.setName("Test"); // Filter dengan 'Test'

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(), any(Map.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // Act
        userService.getAllUsers(PageRequest.of(0, 1), filterDto);

        // Assert
        // Verifikasi bahwa service membangun Map dengan benar sebelum memanggil repository
        verify(userRepository).findAll(any(), mapCaptor.capture());
        Map<String, Object> capturedMap = mapCaptor.getValue();

        // Pastikan service menambahkan wildcard '%' untuk pencarian LIKE
        assertThat(capturedMap).hasSize(1);
        assertThat(capturedMap.get("name")).isEqualTo("%Test%");
    }

    @Test
    void getAllUsers_withNoFilter_shouldCallRepositoryWithEmptyMap() {
        // Arrange
        UserFilterDto emptyFilter = new UserFilterDto(); // DTO filter kosong
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(), any(Map.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // Act
        userService.getAllUsers(PageRequest.of(0, 1), emptyFilter);

        // Assert
        verify(userRepository).findAll(any(), mapCaptor.capture());
        // Pastikan map yang dikirim ke repository kosong
        assertThat(mapCaptor.getValue()).isEmpty();
    }

    @Test
    void createUser_whenRoleExists_shouldSaveUserWithRole() {
        // Arrange
        UserRequestDto requestDto = new UserRequestDto("Test User", "test@example.com", 1L);
        User userToSave = new User("Test User", "test@example.com"); // User tanpa role dari mapper

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(userToSave);
        when(userRepository.save(any(User.class))).thenReturn(user); // Kembalikan user lengkap dengan role
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // Act
        userService.createUser(requestDto);

        // Assert
        verify(roleRepository).findById(1L);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isNotNull();
        assertThat(userCaptor.getValue().getRole().getId()).isEqualTo(1L);
    }

    @Test
    void createUser_whenRoleNotFound_shouldThrowException() {
        // Arrange
        UserRequestDto requestDto = new UserRequestDto("Test User", "test@example.com", 99L);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(requestDto);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_whenUserExists_shouldReturnDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
    }

    @Test
    void updateUser_whenUserAndRoleExist_shouldUpdateAndReturnDto() {
        // Arrange
        UserRequestDto updateDto = new UserRequestDto("Updated Name", "updated@email.com", 2L);
        Role newRole = new Role("USER", "Regular user");
        newRole.setId(2L);
        UserResponseDto updatedResponse = new UserResponseDto(1L, "Updated Name", "updated@email.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(userMapper.toDto(any(User.class))).thenReturn(updatedResponse);

        // Act
        userService.updateUser(1L, updateDto);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Updated Name");
        assertThat(userCaptor.getValue().getRole().getId()).isEqualTo(2L);
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowException() {
        // Arrange
        UserRequestDto updateDto = new UserRequestDto("Updated Name", "updated@email.com", 1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, updateDto);
        });
        verify(roleRepository, never()).findById(anyLong());
    }
}