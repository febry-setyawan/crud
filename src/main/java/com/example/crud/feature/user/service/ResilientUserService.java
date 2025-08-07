package com.example.crud.feature.user.service;

import com.example.crud.feature.user.dto.UserFilterDto;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Primary
@ConditionalOnProperty(
    value="features.resilience.user.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class ResilientUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(ResilientUserService.class);

    private final UserService delegate;

    public ResilientUserService(@Qualifier("defaultUserService") UserService delegate) {
        this.delegate = delegate;
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userDto) {
        return delegate.createUser(userDto);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public UserResponseDto getUserById(Long id) {
        return delegate.getUserById(id);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetAllUsers")
    public Page<UserResponseDto> getAllUsers(Pageable pageable, UserFilterDto filters) {
        return delegate.getAllUsers(pageable, filters);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        return delegate.updateUser(id, userDto);
    }

    @Override
    public boolean deleteUser(Long id) {
        return delegate.deleteUser(id);
    }

    private UserResponseDto fallbackGetUserById(Long id, Throwable t) {
        log.error("Circuit breaker opened for getUserById: {}", id, t);
        return new UserResponseDto(id, "Fallback User", "fallback@example.com", null);
    }

    private Page<UserResponseDto> fallbackGetAllUsers(Pageable pageable, UserFilterDto filters, Throwable t) {
        log.error("Circuit breaker opened for getAllUsers", t);
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
}
