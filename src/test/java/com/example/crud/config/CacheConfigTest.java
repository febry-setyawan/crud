package com.example.crud.config;

import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;
import com.example.crud.feature.role.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
class CacheConfigTest {

    @Autowired
    private RoleService roleService;

    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void findById_shouldCacheRole() {
        // Given
        long roleId = 1L;
        Role role = new Role();
        role.setId(roleId);
        role.setName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // When
        // First call - should hit the database
    roleService.getRoleById(roleId);

        // Second call - should hit the cache
    roleService.getRoleById(roleId);

        // Then
        // Verify that the repository method was only called once
        verify(roleRepository, times(1)).findById(roleId);
    }
}
