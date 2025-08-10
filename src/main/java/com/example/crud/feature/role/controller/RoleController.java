package com.example.crud.feature.role.controller;

import com.example.crud.feature.role.dto.RoleFilterDto;
import com.example.crud.feature.role.dto.RoleRequestDto;
import com.example.crud.feature.role.dto.RoleResponseDto;
import com.example.crud.feature.role.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "Endpoint untuk operasi CRUD pada Role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Membuat role baru", description = "Membuat satu data role baru dan menyimpannya ke database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role berhasil dibuat"),
            @ApiResponse(responseCode = "400", description = "Input tidak valid")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto roleDto) {
        RoleResponseDto createdRole = roleService.createRole(roleDto);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @Operation(summary = "Menampilkan semua role", description = "Mengambil daftar semua role dengan opsi filter, sort, dan pagination.")
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<Page<RoleResponseDto>> getAllRoles(Pageable pageable,
            @RequestParam MultiValueMap<String, String> allParams) {
        RoleFilterDto filter = new RoleFilterDto();
        filter.setName(allParams.getFirst("name"));
        filter.setDescription(allParams.getFirst("description"));

        return ResponseEntity.ok(roleService.getAllRoles(pageable, filter));
    }

    @Operation(summary = "Menampilkan role berdasarkan ID", description = "Mengambil satu data role berdasarkan ID uniknya.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role ditemukan"),
            @ApiResponse(responseCode = "404", description = "Role dengan ID tersebut tidak ditemukan")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @Operation(summary = "Mengupdate role", description = "Memperbarui data role yang sudah ada berdasarkan ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role berhasil diperbarui"),
            @ApiResponse(responseCode = "404", description = "Role dengan ID tersebut tidak ditemukan")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id,
            @Valid @RequestBody RoleRequestDto roleDto) {
        return ResponseEntity.ok(roleService.updateRole(id, roleDto));
    }

    @Operation(summary = "Menghapus role", description = "Menghapus satu data role berdasarkan ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role berhasil dihapus"),
            @ApiResponse(responseCode = "404", description = "Role dengan ID tersebut tidak ditemukan")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (roleService.deleteRole(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}