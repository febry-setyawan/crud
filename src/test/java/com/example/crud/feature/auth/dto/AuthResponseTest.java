package com.example.crud.feature.auth.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {
    @Test
    void constructorAndGetterSetter_shouldWork() {
        AuthResponse response = new AuthResponse("token123");
        assertThat(response.getToken()).isEqualTo("token123");
        response.setToken("newToken");
        assertThat(response.getToken()).isEqualTo("newToken");
    }
}
