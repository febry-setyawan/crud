package com.example.crud.feature.user.dto;

import com.example.crud.feature.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toEntity() {
        // Given
        UserRequestDto userRequestDto = new UserRequestDto("test@example.com", "password", 1L);

        // When
        User user = userMapper.toEntity(userRequestDto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(userRequestDto.username());
        assertThat(user.getPassword()).isEqualTo(userRequestDto.password());
    }

    @Test
    void toDto() {
        // Given
        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("password");

        // When
        UserResponseDto userResponseDto = userMapper.toDto(user);

        // Then
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.username()).isEqualTo(user.getUsername());
        assertThat(userResponseDto.password()).isEqualTo(user.getPassword());
    }
}
