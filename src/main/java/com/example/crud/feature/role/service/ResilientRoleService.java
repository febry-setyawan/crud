package com.example.crud.feature.role.service;

import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Primary
@ConditionalOnProperty(
    value="features.resilience.role.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class ResilientRoleService implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(ResilientRoleService.class);

    private final RoleService delegate;

    public ResilientRoleService(@Qualifier("defaultRoleService") RoleService delegate) {
        this.delegate = delegate;
    }

    @Override
    public RoleResponseDto createRole(RoleRequestDto roleDto) {
        return delegate.createRole(roleDto);
    }

    @Override
    @CircuitBreaker(name = "roleService", fallbackMethod = "fallbackGetRoleById")
    public RoleResponseDto getRoleById(Long id) {
        return delegate.getRoleById(id);
    }

    @Override
    @CircuitBreaker(name = "roleService", fallbackMethod = "fallbackGetAllRoles")
    public Page<RoleResponseDto> getAllRoles(Pageable pageable, RoleFilterDto filter) {
        return delegate.getAllRoles(pageable, filter);
    }

    @Override
    public RoleResponseDto updateRole(Long id, RoleRequestDto roleDto) {
        return delegate.updateRole(id, roleDto);
    }

    @Override
    public boolean deleteRole(Long id) {
        return delegate.deleteRole(id);
    }

    private RoleResponseDto fallbackGetRoleById(Long id, Throwable t) {
        log.error("Circuit breaker opened for getRoleById: {}", id, t);
        return new RoleResponseDto(id, "Fallback Role", "Service is currently unavailable");
    }

    private Page<RoleResponseDto> fallbackGetAllRoles(Pageable pageable, RoleFilterDto filters, Throwable t) {
        log.error("Circuit breaker opened for getAllRoles", t);
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
}