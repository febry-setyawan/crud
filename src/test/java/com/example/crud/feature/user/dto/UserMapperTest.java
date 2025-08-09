package com.example.crud.feature.user.dto;

import com.example.crud.feature.user.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity() {
        // Given
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("Test User");
        userRequestDto.setEmail("test@example.com");
        userRequestDto.setPassword("password");
        userRequestDto.setRoleId(1L);

        // When
        User user = userMapper.toEntity(userRequestDto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(userRequestDto.getName());
        assertThat(user.getEmail()).isEqualTo(userRequestDto.getEmail());
        assertThat(user.getPassword()).isEqualTo(userRequestDto.getPassword());
    }

    @Test
    void toDto() {
        // Given
        User user = new User();
        user.setName("Test User");

        // When
        UserResponseDto userResponseDto = userMapper.toDto(user);

        // Then
        assertThat(userResponseDto).isNotNull();
