package com.example.crud.common.model;

import java.time.LocalDateTime;

public abstract class BaseEntityAuditable<T> extends BaseEntity<T> implements Auditable {

    protected LocalDateTime createdAt;
    protected String createdBy;
    protected LocalDateTime updatedAt;
    protected String updatedBy;

    // Getters
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }

    // Implementasi dari Auditable
    @Override
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @Override
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    @Override
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
