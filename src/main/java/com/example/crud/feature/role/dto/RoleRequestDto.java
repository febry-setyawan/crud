package com.example.crud.feature.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequestDto(
    @NotBlank(message = "Role name is mandatory")
    @Size(min = 2, max = 100)
    String name,

    @Size(max = 255)
    String description
) {}