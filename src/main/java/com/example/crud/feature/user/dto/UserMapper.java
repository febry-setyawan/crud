package com.example.crud.feature.user.dto;

import com.example.crud.feature.role.dto.RoleMapper;
import com.example.crud.feature.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toEntity(UserRequestDto userRequestDto);

    UserResponseDto toDto(User user);
}