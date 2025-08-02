package com.example.crud.feature.user.controller;

import com.example.crud.feature.user.service.UserService;
import com.example.crud.feature.user.dto.UserFilterDto;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoint untuk operasi CRUD pada User")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Membuat user baru", description = "Membuat satu data user baru dan menyimpannya ke database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Input tidak valid")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userDto) {
        UserResponseDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Menampilkan semua user", description = "Mengambil daftar semua user dengan opsi filter, sort, dan pagination.")
    @PageableAsQueryParam 
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable, @RequestParam MultiValueMap<String, String> allParams) {
        UserFilterDto filter = new UserFilterDto();
        filter.setName(allParams.getFirst("name"));
        filter.setEmail(allParams.getFirst("email"));

        Page<UserResponseDto> page = userService.getAllUsers(pageable, filter);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Menampilkan user berdasarkan ID", description = "Mengambil satu data user berdasarkan ID uniknya.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User ditemukan"),
        @ApiResponse(responseCode = "404", description = "User dengan ID tersebut tidak ditemukan")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Mengupdate user", description = "Memperbarui data user yang sudah ada berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User berhasil diperbarui"),
        @ApiResponse(responseCode = "404", description = "User dengan ID tersebut tidak ditemukan")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @Operation(summary = "Menghapus user", description = "Menghapus satu data user berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User berhasil dihapus"),
        @ApiResponse(responseCode = "404", description = "User dengan ID tersebut tidak ditemukan")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}