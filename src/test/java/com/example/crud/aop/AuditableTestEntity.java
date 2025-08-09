package com.example.crud.aop;

import com.example.crud.common.model.BaseEntityAuditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("test_auditable_entity")
public class AuditableTestEntity extends BaseEntityAuditable {
    private String name;

    public AuditableTestEntity(Long id, String name) {
        this.setId(id);
        this.name = name;
    }
