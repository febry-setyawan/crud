package com.example.crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Nonaktifkan CSRF untuk REST API
            .authorizeHttpRequests(auth -> auth
                // Izinkan akses publik ke Swagger UI dan H2 Console
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                // Semua request lain harus terotentikasi
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults()) // Aktifkan HTTP Basic Authentication
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Izinkan H2 Console di dalam frame

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Buat user sederhana di memori untuk testing
        @SuppressWarnings("deprecation")
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}