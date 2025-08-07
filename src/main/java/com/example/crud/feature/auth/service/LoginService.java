package com.example.crud.feature.auth.service;

import com.example.crud.feature.user.model.User;
import com.example.crud.feature.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Map;

@Service
public class LoginService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findAll(PageRequest.of(0, 1), Map.of("username", username))
            .getContent().stream().findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singleton(authority))
                .build();
    }
}
