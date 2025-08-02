package com.example.crud.feature.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.example.crud.common.exception.ResourceNotFoundException;
import com.example.crud.feature.role.dto.RoleResponseDto; // <-- Import Role DTO
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import com.example.crud.feature.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        JdbcRepositoriesAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        AopAutoConfiguration.class
    }
)
@WithMockUser(username = "testuser", roles = "USER")
class UserControllerTest {

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
        userRequestDto = new UserRequestDto("Test User", "test@example.com", 1L);
        userResponseDto = new UserResponseDto(1L, "Test User", "test@example.com", roleDto);
        updatedUserResponseDto = new UserResponseDto(1L, "Updated User", "updated@email.com", roleDto);

        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));

        // --- Definisikan Perilaku Mock untuk Setiap Skenario ---
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);
        when(userService.getAllUsers(any(Pageable.class), anyMap())).thenReturn(userPage);
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
                .andExpect(jsonPath("$.name", is("Test User")))
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
                .andExpect(jsonPath("$.name", is("Test User")))
                // Verifikasi data role
                .andExpect(jsonPath("$.role.name", is("ADMIN")));
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUserWithRole() throws Exception {
        // Buat request DTO baru untuk update
        UserRequestDto updateRequest = new UserRequestDto("Updated User", "updated@email.com", 1L);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated User")))
                .andExpect(jsonPath("$.role.name", is("ADMIN")));
    }

    // --- Test untuk skenario "Not Found" dan "Delete" tidak perlu diubah ---

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/users/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}