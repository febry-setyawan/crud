package com.example.crud.feature.auth.controller;

import com.example.crud.feature.auth.dto.AuthRequest;
import com.example.crud.feature.auth.dto.RefreshRequest;
import com.example.crud.feature.auth.dto.RefreshResponse;
import com.example.crud.feature.auth.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoint untuk login, refresh, dan logout JWT")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @Operation(summary = "Login user", description = "Login dan mendapatkan access token serta refresh token.")
    @ApiResponse(responseCode = "200", description = "Login berhasil")
    @ApiResponse(responseCode = "401", description = "Login gagal")
    @PostMapping("/login")
    public RefreshResponse login(@RequestBody AuthRequest authRequest) {
        return authenticationService.login(authRequest);
    }


    @Operation(summary = "Refresh token", description = "Mendapatkan access token baru dengan refresh token.")
    @ApiResponse(responseCode = "200", description = "Refresh berhasil")
    @ApiResponse(responseCode = "400", description = "Refresh token tidak valid")
    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        return authenticationService.refresh(request.getRefreshToken());
    }


    @Operation(summary = "Logout", description = "Menghapus refresh token dari cache.")
    @ApiResponse(responseCode = "200", description = "Logout berhasil")
    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest request) {
        authenticationService.logout(request.getRefreshToken());
    }
}
