package com.example.crud.feature.user.model;


import com.example.crud.common.model.BaseEntityAuditable;
import com.example.crud.feature.role.model.Role;

public class User extends BaseEntityAuditable<Long> {
    private String name;
    private String email;
    private Role role;

    // Constructors, Getters, Setters
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

} 