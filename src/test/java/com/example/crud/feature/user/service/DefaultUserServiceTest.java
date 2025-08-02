package com.example.crud.feature.user.service;

import com.example.crud.feature.user.dto.UserMapper;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import com.example.crud.feature.user.model.User;
import com.example.crud.feature.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.crud.common.exception.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserService userService;
    private User user;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        userService = new DefaultUserService(userRepository, userMapper);
        user = new User("Test User", "test@example.com");
        user.setId(1L);
        responseDto = new UserResponseDto(1L, "Test User", "test@example.com");
    }

    @Test
    void createUser_shouldMapAndSave() {

        UserRequestDto requestDto = new UserRequestDto("Test User", "test@example.com");
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // Act
        UserResponseDto result = userService.createUser(requestDto);

        // Assert
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllUsers_shouldFetchPageAndMapToDto() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(), any(Map.class))).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // Act
        // Gunakan new HashMap<>() bukan Map.of()
        Page<UserResponseDto> resultPage = userService.getAllUsers(PageRequest.of(0, 1), new HashMap<>());

        // Assert
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().get(0).name()).isEqualTo("Test User");
    }

    @Test
    void getUserById_whenUserExists_shouldReturnDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserResponseDto(1L, "Test User", "test@example.com"));

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
        // Verifikasi bahwa eksepsi dilempar saat user tidak ditemukan
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateAndReturnDto() {
        // Arrange
        UserRequestDto updateRequestDto = new UserRequestDto("Updated Name", "updated@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        userService.updateUser(1L, updateRequestDto);

        // Assert
        // Verifikasi bahwa service mencoba menyimpan user yang sudah di-update
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        UserRequestDto updateRequestDto = new UserRequestDto("Updated Name", "updated@email.com");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, updateRequestDto);
        });
    }
}