package com.example.crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                request -> request.getRequestURI().startsWith("/h2-console"), // Nonaktifkan CSRF untuk H2 Console
                request -> request.getRequestURI().startsWith("/actuator"), // Nonaktifkan CSRF untuk actuator
                request -> request.getRequestURI().startsWith("/swagger-ui"), // Nonaktifkan CSRF untuk Swagger UI
                request -> request.getRequestURI().startsWith("/v3/api-docs") // Nonaktifkan CSRF untuk OpenAPI docs
            ))
            .authorizeHttpRequests(auth -> auth
                // Izinkan akses publik ke Swagger UI dan H2 Console
                .requestMatchers(
                    "/swagger-ui/**", 
                    "/v3/api-docs/**", 
                    "/h2-console/**",
                    "/actuator/**"
                ).permitAll()
                // Semua request lain harus terotentikasi
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults()) // Aktifkan HTTP Basic Authentication
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin)); // Izinkan H2 Console di dalam frame

        return http.build();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Buat user sederhana di memori untuk testing
        String password = RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom());
        log.debug("Generated password for user: {}", password);
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password(password)
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}