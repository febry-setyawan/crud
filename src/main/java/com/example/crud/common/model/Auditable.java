package com.example.crud.common.model;

import java.time.LocalDateTime;

public interface Auditable {

    void setCreatedAt(LocalDateTime createdAt);

    void setCreatedBy(String createdBy);

    void setUpdatedAt(LocalDateTime updatedAt);

    void setUpdatedBy(String updatedBy);
}