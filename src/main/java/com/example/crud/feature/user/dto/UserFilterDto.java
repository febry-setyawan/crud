package com.example.crud.feature.user.dto;


import com.example.crud.feature.role.model.Role;

public class UserFilterDto {
    private String username;
    private String password;
    private Role role;

    // Getters and Setters
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}