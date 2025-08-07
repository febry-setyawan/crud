package com.example.crud.feature.auth.controller;

import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;






import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")

class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpUser() throws Exception {
        // Log metadata kolom tabel users
        jdbcTemplate.query("SELECT * FROM users WHERE 1=0", rs -> {
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            System.out.println("[DEBUG] Kolom tabel users:");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.println("[DEBUG] - " + meta.getColumnName(i));
            }
            return null;
        });
        // Hapus user jika sudah ada
        jdbcTemplate.update("DELETE FROM users WHERE username = ?", "admin@email.com");
        // Pastikan role ADMIN ada
        Long roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", new Object[]{"ADMIN"}, Long.class);
        // Insert user dengan password bcrypt
        String bcrypt = new BCryptPasswordEncoder().encode("s3cr3t");
        jdbcTemplate.update("INSERT INTO users (id, username, password, role_id) VALUES (?, ?, ?, ?)", 100L, "admin@email.com", bcrypt, roleId);
    }

    @Test
    void login_refresh_logout_flow() throws Exception {
        // 1. Login
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("admin@email.com"); // Use a valid username from your test config
        loginRequest.setPassword("s3cr3t"); // Use a valid password from your test config
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginJson = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginJson).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginJson).get("refreshToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // 2. Refresh
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String refreshJson = refreshResult.getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(refreshJson).get("accessToken").asText();
        String newRefreshToken = objectMapper.readTree(refreshJson).get("refreshToken").asText();
        assertThat(newAccessToken).isNotBlank();
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newRefreshToken).isNotEqualTo(refreshToken); // Rotation

        // 3. Logout
        RefreshRequest logoutRequest = new RefreshRequest();
        logoutRequest.setRefreshToken(newRefreshToken);
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 4. Refresh with logged out token should fail
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().is4xxClientError());
    }
}
