package com.example.crud.feature.user.model;

import com.example.crud.common.model.BaseEntityAuditable;

public class User extends BaseEntityAuditable<Long> {
    private String name;
    private String email;

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

} 