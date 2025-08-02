package com.example.crud.feature.role.service;

import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto roleDto);
    RoleResponseDto getRoleById(Long id);
    Page<RoleResponseDto> getAllRoles(Pageable pageable, RoleFilterDto filter);
    RoleResponseDto updateRole(Long id, RoleRequestDto roleDto);
    boolean deleteRole(Long id);
}