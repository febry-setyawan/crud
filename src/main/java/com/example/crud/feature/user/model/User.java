package com.example.crud.feature.user.model;


import com.example.crud.common.model.BaseEntityAuditable;
import com.example.crud.feature.role.model.Role;

public class User extends BaseEntityAuditable<Long> {
    private String username;
    private String password;
    private Role role;

    // Constructors, Getters, Setters
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}