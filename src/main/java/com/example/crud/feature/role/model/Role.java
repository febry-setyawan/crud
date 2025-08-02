package com.example.crud.feature.role.model;

import com.example.crud.common.model.BaseEntity;

public class Role extends BaseEntity<Long> {
    private String name;
    private String description;

    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

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
