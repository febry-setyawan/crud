package com.example.crud.feature.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
    @NotBlank(message = "Username is mandatory")
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    @Email(message = "Username must be a valid email format")
    String username,

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters")
    String password,

    @NotNull(message = "Role ID is mandatory")
    Long roleId
) {}