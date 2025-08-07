package com.example.crud.feature.user.dto;


import com.example.crud.feature.role.dto.RoleResponseDto;

public record UserResponseDto(Long id, String name, String email, RoleResponseDto role) {}