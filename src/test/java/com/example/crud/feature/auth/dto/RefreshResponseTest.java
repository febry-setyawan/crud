package com.example.crud.feature.auth.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshResponseTest {
    @Test
    void constructorAndGetterSetter_shouldWork() {
        RefreshResponse response = new RefreshResponse("access", "refresh");
        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        response.setAccessToken("newAccess");
        response.setRefreshToken("newRefresh");
        assertThat(response.getAccessToken()).isEqualTo("newAccess");
        assertThat(response.getRefreshToken()).isEqualTo("newRefresh");
    }
}
