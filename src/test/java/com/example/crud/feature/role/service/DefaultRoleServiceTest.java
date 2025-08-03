package com.example.crud.feature.role.service;

import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.dto.RoleMapper;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    private RoleService roleService;
    private Role role;
    private RoleResponseDto responseDto;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @BeforeEach
    void setUp() {
        roleService = new DefaultRoleService(roleRepository, roleMapper);
        role = new Role("ADMIN", "Admin role");
        role.setId(1L);
        responseDto = new RoleResponseDto(1L, "ADMIN", "Admin role");
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllRoles_withFilter_shouldBuildMapWithWildcardsAndCallRepository() {
        // Arrange
        RoleFilterDto filterDto = new RoleFilterDto();
        filterDto.setName("admin"); // Filter dengan 'admin'

        Page<Role> rolePage = new PageImpl<>(List.of(role));
        when(roleRepository.findAll(any(), any(Map.class))).thenReturn(rolePage);
        when(roleMapper.toDto(any(Role.class))).thenReturn(responseDto);

        // Act
        roleService.getAllRoles(PageRequest.of(0, 1), filterDto);

        // Assert
        // Verifikasi bahwa service membangun Map dengan benar sebelum memanggil repository
        verify(roleRepository).findAll(any(), mapCaptor.capture());
        Map<String, Object> capturedMap = mapCaptor.getValue();

        // Pastikan service menambahkan wildcard '%' untuk pencarian LIKE
        assertThat(capturedMap).hasSize(1);
        assertThat(capturedMap.get("name")).isEqualTo("%admin%");
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllRoles_withNoFilter_shouldCallRepositoryWithEmptyMap() {
        // Arrange
        RoleFilterDto emptyFilter = new RoleFilterDto(); // DTO filter kosong
        Page<Role> rolePage = new PageImpl<>(List.of(role));
        when(roleRepository.findAll(any(), any(Map.class))).thenReturn(rolePage);
        when(roleMapper.toDto(any(Role.class))).thenReturn(responseDto);

        // Act
        roleService.getAllRoles(PageRequest.of(0, 1), emptyFilter);

        // Assert
        verify(roleRepository).findAll(any(), mapCaptor.capture());
        // Pastikan map yang dikirim ke repository kosong
        assertThat(mapCaptor.getValue()).isEmpty();
    }

    @Test
    void createRole_shouldMapAndSave() {
        // Arrange
        RoleRequestDto requestDto = new RoleRequestDto("ADMIN", "Admin role");
        when(roleMapper.toEntity(any(RoleRequestDto.class))).thenReturn(role);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toDto(any(Role.class))).thenReturn(responseDto);

        // Act
        RoleResponseDto result = roleService.createRole(requestDto);

        // Assert
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getRoleById_whenRoleExists_shouldReturnDto() {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toDto(role)).thenReturn(responseDto);

        // Act
        RoleResponseDto result = roleService.getRoleById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getRoleById_whenRoleDoesNotExist_shouldThrowException() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            roleService.getRoleById(99L);
        });
    }

    @Test
    void updateRole_whenRoleExists_shouldUpdateAndReturnDto() {
        // Arrange
        RoleRequestDto updateRequestDto = new RoleRequestDto("Updated Name", "Updated desc");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toDto(any(Role.class))).thenReturn(new RoleResponseDto(1L, "Updated Name", "Updated desc"));

        // Act
        RoleResponseDto result = roleService.updateRole(1L, updateRequestDto);
        
        // Assert
        assertThat(result.name()).isEqualTo("Updated Name");

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).update(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateRole_whenRoleDoesNotExist_shouldThrowException() {
        // Arrange
        RoleRequestDto updateRequestDto = new RoleRequestDto("Updated Name", "Updated desc");
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            roleService.updateRole(99L, updateRequestDto);
        });
    }

    @Test
    void deleteRole_whenDeleteSucceeds_shouldReturnTrue() {
        // Arrange
        when(roleRepository.deleteById(1L)).thenReturn(1); // 1 means 1 row deleted

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void deleteRole_whenRoleNotFound_shouldReturnFalse() {
        // Arrange
        when(roleRepository.deleteById(99L)).thenReturn(0); // 0 means 0 rows deleted

        // Act
        boolean result = roleService.deleteRole(99L);

        // Assert
        assertThat(result).isFalse();
    }
}