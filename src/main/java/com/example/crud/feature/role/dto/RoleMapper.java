package com.example.crud.feature.role.dto;

import com.example.crud.feature.role.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    Role toEntity(RoleRequestDto roleRequestDto);

    RoleResponseDto toDto(Role role);
}