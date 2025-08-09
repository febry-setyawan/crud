package com.example.crud.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/runtime-exception")
    public void throwRuntimeException() {
        throw new RuntimeException("This is a test runtime exception");
    }

    @GetMapping("/test/resource-not-found")
    public void throwResourceNotFoundException() {
        throw new ResourceNotFoundException("Test resource not found");
    }

    @PostMapping("/test/validation-error")
    public ResponseEntity<String> testValidationError(@Valid @RequestBody TestDto testDto) {
        return ResponseEntity.ok("Valid");
    }

    @Data
    public static class TestDto {
        @NotBlank
        private String name;
