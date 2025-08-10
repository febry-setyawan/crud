package com.example.crud.config;

import com.example.crud.feature.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService; // Mock JwtService to avoid JWT validation issues in tests

    @BeforeEach
    void setupJwtServiceMock() {
        // Return dummy tokens for any username
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.anyString())).thenReturn("dummy-access-token");
        when(jwtService.generateRefreshToken(org.mockito.ArgumentMatchers.anyString())).thenReturn("dummy-refresh-token");
    }

    @Test
    void shouldAllowPublicAccessToSwagger() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicAccessToApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicAccessToH2Console() throws Exception {
        mockMvc.perform(get("/h2-console/"))
            // H2 console resource tidak tersedia di test context, bisa 404 atau 2xx jika tersedia
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status != 200 && status != 404) {
                    throw new AssertionError("Expected status 200 or 404, got: " + status);
                }
            });
    }

    @Test
    void shouldAllowPublicAccessToActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicAccessToAuthEndpoints() throws Exception {
    // Tambahkan CSRF agar lolos filter CSRF
    mockMvc.perform(post("/api/auth/login")
        .contentType("application/json")
        .content("{\"username\":\"user@email.com\",\"password\":\"s3cr3t\"}")
        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
    )
    .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Simulates an authenticated admin user
    void shouldAllowAccessToProtectedEndpoints_whenAuthenticated() throws Exception {
        // Always include a valid roleId to avoid validation errors
        mockMvc.perform(get("/api/users?roleId=1"))
                .andExpect(status().isOk());
    }
}
