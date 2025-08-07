package com.example.crud.feature.user.dto;


import com.example.crud.feature.role.dto.RoleResponseDto;

public record UserResponseDto(Long id, String username, String password, RoleResponseDto role) {}