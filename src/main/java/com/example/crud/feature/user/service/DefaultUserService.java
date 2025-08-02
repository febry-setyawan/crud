package com.example.crud.feature.user.service;

import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.role.repository.RoleRepository;
import com.example.crud.feature.user.dto.UserMapper;
import com.example.crud.feature.user.dto.UserRequestDto;
import com.example.crud.feature.user.dto.UserResponseDto;
import com.example.crud.feature.user.model.User;
import com.example.crud.feature.user.repository.UserRepository;
import com.example.crud.common.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service("defaultUserService")
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public DefaultUserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto) {
        Role role = roleRepository.findById(userDto.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userDto.roleId()));
                
        User user = userMapper.toEntity(userDto);
        user.setRole(role);

        User savedUser = userRepository.save(user); // AOP audit trail tetap berjalan di sini
        return userMapper.toDto(savedUser);
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    public Page<UserResponseDto> getAllUsers(Pageable pageable, Map<String, Object> filters) {
        Map<String, Object> mutableFilters = (filters != null) ? new HashMap<>(filters) : new HashMap<>();

        mutableFilters.remove("page");
        mutableFilters.remove("size");
        mutableFilters.remove("sort");

        Page<User> userPage = userRepository.findAll(pageable, mutableFilters);
        return userPage.map(userMapper::toDto);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Validasi Role baru jika ada perubahan
        Role role = roleRepository.findById(userDto.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userDto.roleId()));

        existingUser.setName(userDto.name());
        existingUser.setEmail(userDto.email());
        existingUser.setRole(role); // Update Role
        
        userRepository.update(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id) > 0;
    }
}