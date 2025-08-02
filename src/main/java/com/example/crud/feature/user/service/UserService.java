package com.example.crud.feature.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.crud.feature.user.dto.UserFilterDto;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userDto);
    UserResponseDto getUserById(Long id);
    Page<UserResponseDto> getAllUsers(Pageable pageable, UserFilterDto userFilterDto);
    UserResponseDto updateUser(Long id, UserRequestDto userDto);
    boolean deleteUser(Long id);
}