package com.example.crud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.crud.feature.user.repository.UserRepository;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.crud.feature.auth.filter.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        logger.debug("Creating DaoAuthenticationProvider with UserRepository={} and PasswordEncoder={}", userRepository.getClass().getName(), passwordEncoder.getClass().getName());
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userRepository);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
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
                    "/actuator/**",
                    "/api/auth/**"
                ).permitAll()
                // Semua request lain harus terotentikasi
                .anyRequest().authenticated()
            )
            // Nonaktifkan HTTP Basic Authentication, pakai JWT
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin)); // Izinkan H2 Console di dalam frame

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating PasswordEncoder: BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("Creating AuthenticationManager from AuthenticationConfiguration: {}", authenticationConfiguration.getClass().getName());
        return authenticationConfiguration.getAuthenticationManager();
    }
}