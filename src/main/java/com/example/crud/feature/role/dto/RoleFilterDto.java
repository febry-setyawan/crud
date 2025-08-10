package com.example.crud.feature.role.dto;

// Tidak perlu anotasi, ini hanya object data biasa
public class RoleFilterDto {
    private String name;
    private String description;

    // Buat getter dan setter untuk semua field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}