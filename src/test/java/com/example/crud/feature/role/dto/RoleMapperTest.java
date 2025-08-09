package com.example.crud.feature.role.dto;

import com.example.crud.feature.role.model.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    @Test
    void toEntity() {
        // Given
        RoleRequestDto roleRequestDto = new RoleRequestDto();
        roleRequestDto.setName("Test Role");

        // When
        Role role = roleMapper.toEntity(roleRequestDto);

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(roleRequestDto.getName());
    }

    @Test
    void toDto() {
        // Given
        Role role = new Role();
        role.setName("Test Role");

        // When
        RoleResponseDto roleResponseDto = roleMapper.toDto(role);

        // Then
        assertThat(roleResponseDto).isNotNull();
        assertThat(roleResponseDto.getName()).isEqualTo(role.getName());
