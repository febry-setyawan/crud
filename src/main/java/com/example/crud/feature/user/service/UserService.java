package com.example.crud.feature.user.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userDto);
    UserResponseDto getUserById(Long id);
    Page<UserResponseDto> getAllUsers(Pageable pageable, Map<String, Object> filters);
    UserResponseDto updateUser(Long id, UserRequestDto userDto);
    boolean deleteUser(Long id);
}