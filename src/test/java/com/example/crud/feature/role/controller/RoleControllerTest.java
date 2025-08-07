package com.example.crud.feature.role.controller;

import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.role.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @SuppressWarnings("removal")
    @MockBean
    private RoleService roleService;


    @Autowired
    private ObjectMapper objectMapper;

    private RoleRequestDto roleRequestDto;
    private RoleResponseDto roleResponseDto;
    private RoleResponseDto updatedRoleResponseDto;

    @BeforeEach
    void setUp() {
        // Prepare DTO objects for tests
        roleRequestDto = new RoleRequestDto("ADMIN", "Administrator role");
        roleResponseDto = new RoleResponseDto(1L, "ADMIN", "Administrator role");
        updatedRoleResponseDto = new RoleResponseDto(1L, "SUPER_ADMIN", "Super Administrator role");

        Page<RoleResponseDto> rolePage = new PageImpl<>(List.of(roleResponseDto));

        // --- Define Mock Behaviors for Every Scenario ---

        // Happy Path Scenarios
        when(roleService.createRole(any(RoleRequestDto.class))).thenReturn(roleResponseDto);
        when(roleService.getAllRoles(any(Pageable.class), any(RoleFilterDto.class))).thenReturn(rolePage);
        when(roleService.getRoleById(1L)).thenReturn(roleResponseDto);
        when(roleService.updateRole(eq(1L), any(RoleRequestDto.class))).thenReturn(updatedRoleResponseDto);
        when(roleService.deleteRole(1L)).thenReturn(true);

        // Sad Path Scenarios
        when(roleService.getRoleById(99L)).thenThrow(new ResourceNotFoundException("Role not found with id: 99"));
        when(roleService.updateRole(eq(99L), any(RoleRequestDto.class))).thenThrow(new ResourceNotFoundException("Role not found with id: 99"));
        when(roleService.deleteRole(99L)).thenReturn(false);
    }

    @Test
    void createRole_shouldReturnCreatedRole() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    void getAllRoles_shouldReturnPageOfRoles() throws Exception {
        mockMvc.perform(get("/api/roles?page=0&size=5&name=ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    void getRoleById_whenRoleExists_shouldReturnRole() throws Exception {
        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    void getRoleById_whenRoleDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Role not found with id: 99")));
    }

    @Test
    void updateRole_whenRoleExists_shouldReturnUpdatedRole() throws Exception {
        mockMvc.perform(put("/api/roles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequestDto("SUPER_ADMIN", "Super Administrator role"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("SUPER_ADMIN")));
    }

    @Test
    void updateRole_whenRoleDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/roles/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Role not found with id: 99")));
    }

    @Test
    void deleteRole_whenRoleExists_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/roles/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRole_whenRoleDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/roles/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}