package com.example.crud.feature.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.example.crud.common.exception.ResourceNotFoundException;
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
        // Siapkan objek DTO untuk digunakan di semua test
        userRequestDto = new UserRequestDto("Test User", "test@example.com");
        userResponseDto = new UserResponseDto(1L, "Test User", "test@example.com");
        updatedUserResponseDto = new UserResponseDto(1L, "Updated User", "updated@email.com");
        
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(userResponseDto));

        // --- Definisikan Perilaku Mock untuk Setiap Skenario ---

        // Skenario Sukses (Happy Path)
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);
        when(userService.getAllUsers(any(Pageable.class), anyMap())).thenReturn(userPage);
        when(userService.getUserById(1L)).thenReturn(userResponseDto);
        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(updatedUserResponseDto);
        when(userService.deleteUser(1L)).thenReturn(true);

        // Skenario Gagal (Sad Path)
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));
        when(userService.updateUser(eq(99L), any(UserRequestDto.class))).thenThrow(new ResourceNotFoundException("User not found with id: 99"));
        when(userService.deleteUser(99L)).thenReturn(false);
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsers() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with id: 99")));
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUser() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRequestDto("Updated User", "updated@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated User")));
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
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))         
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/99")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}