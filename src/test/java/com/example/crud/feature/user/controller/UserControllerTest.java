package com.example.crud.feature.user.controller;


import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.user.dto.UserFilterDto;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import com.example.crud.feature.user.service.UserService;
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
class UserControllerTest {
    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_withNonAdminRole_shouldReturnForbidden() throws Exception {
        // Mock service to return a valid page so validation passes
        when(userService.getAllUsers(any(Pageable.class), any(UserFilterDto.class)))
            .thenReturn(new PageImpl<>(List.of()));
        var result = mockMvc.perform(get("/api/users?roleId=1"))
            .andReturn();
        System.out.println("DEBUG Forbidden Test Response: " + result.getResponse().getContentAsString());
        // Still assert forbidden for test result
        org.springframework.test.util.AssertionErrors.assertEquals(
            "Status expected:<403> but was:<" + result.getResponse().getStatus() + ">",
            403, result.getResponse().getStatus());
    }
    @Test
    void getAllUsers_withInvalidRoleId_shouldReturnBadRequest() throws Exception {
        // roleId string, harusnya gagal validasi (bad request)
        mockMvc.perform(get("/api/users?roleId=abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_withNullRoleId_shouldReturnAllUsers() throws Exception {
        // roleId null, harusnya return semua user (default behavior)
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getAllUsers(any(Pageable.class), any(UserFilterDto.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
    @Test
    void getAllUsers_withUsernameAndRoleIdFilter_shouldReturnFilteredUsers() throws Exception {
        // Simulasi kombinasi filter username dan roleId
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));
        when(userService.getAllUsers(any(Pageable.class), any(UserFilterDto.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users?username=admin@email.com&roleId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("admin@email.com")))
                .andExpect(jsonPath("$.content[0].role.id", is(1)));
    }
    @Test
    void getAllUsers_withNonExistentRoleId_shouldReturnEmptyList() throws Exception {
        // Simulasi roleId yang tidak ada (misal, 999)
        Page<UserResponseDto> emptyPage = new PageImpl<>(List.of());
        when(userService.getAllUsers(any(Pageable.class), any(UserFilterDto.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users?roleId=999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }


    @Test
    void getAllUsers_withRoleIdFilter_shouldReturnFilteredUsers() throws Exception {
        // Simulasi filter roleId=1
        mockMvc.perform(get("/api/users?roleId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].role.id", is(1)))
                .andExpect(jsonPath("$.content[0].role.name", is("ADMIN")));
    }


    @Autowired
    private MockMvc mockMvc;


    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private UserResponseDto updatedUserResponseDto;

    @BeforeEach
    void setUp() {
        // Siapkan DTO untuk Role
        RoleResponseDto roleDto = new RoleResponseDto(1L, "ADMIN", "Administrator");

        // Perbarui DTO User untuk menyertakan roleId dan RoleResponseDto
        userRequestDto = new UserRequestDto("admin@email.com", "s3cr3t", 1L);
        userResponseDto = new UserResponseDto(1L, "admin@email.com", "s3cr3t", roleDto);
        updatedUserResponseDto = new UserResponseDto(1L, "updated@email.com", "new-s3cr3t", roleDto);

        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));

        // --- Definisikan Perilaku Mock untuk Setiap Skenario ---
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);
        when(userService.getAllUsers(any(Pageable.class), any(UserFilterDto.class))).thenReturn(userPage);
        when(userService.getUserById(1L)).thenReturn(userResponseDto);
        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(updatedUserResponseDto);
        when(userService.deleteUser(1L)).thenReturn(true);
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));
        when(userService.updateUser(eq(99L), any(UserRequestDto.class))).thenThrow(new ResourceNotFoundException("User not found with id: 99"));
        when(userService.deleteUser(99L)).thenReturn(false);
    }

    @Test
    void createUser_shouldReturnCreatedUserWithRole() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin@email.com")))
                // Verifikasi data role di response
                .andExpect(jsonPath("$.role.name", is("ADMIN")));
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsersWithRole() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                // Verifikasi data role di dalam list
                .andExpect(jsonPath("$.content[0].role.id", is(1)));
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUserWithRole() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin@email.com")))
                // Verifikasi data role
                .andExpect(jsonPath("$.role.name", is("ADMIN")));
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUserWithRole() throws Exception {
        // Buat request DTO baru untuk update
        UserRequestDto updateRequest = new UserRequestDto("admin@email.com", "new-s3cr3t", 1L);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updated@email.com")))
                .andExpect(jsonPath("$.role.name", is("ADMIN")));
    }

    // --- Test untuk skenario "Not Found" dan "Delete" tidak perlu diubah ---


    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with id: 99")));
    }


    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/users/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with id: 99")));
    }

    @Test
    void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with id: 99")));
    }
}