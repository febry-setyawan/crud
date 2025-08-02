package com.example.crud.feature.role.service;

import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleMapper;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service("defaultRoleService")
public class DefaultRoleService implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public DefaultRoleService(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleRequestDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public RoleResponseDto getRoleById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @Override
    public Page<RoleResponseDto> getAllRoles(Pageable pageable, Map<String, Object> filters) {
        Map<String, Object> mutableFilters = (filters != null) ? new HashMap<>(filters) : new HashMap<>();
        mutableFilters.remove("page");
        mutableFilters.remove("size");
        mutableFilters.remove("sort");

        Page<Role> rolePage = roleRepository.findAll(pageable, mutableFilters);
        return rolePage.map(roleMapper::toDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public RoleResponseDto updateRole(Long id, RoleRequestDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        existingRole.setName(roleDto.name());
        existingRole.setDescription(roleDto.description());
        roleRepository.update(existingRole);
        return roleMapper.toDto(existingRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public boolean deleteRole(Long id) {
        return roleRepository.deleteById(id) > 0;
    }
}