package com.finflow.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.finflow.application.security.JwtFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 🔥 SWAGGER (VERY IMPORTANT)
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/*/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // 🔓 PUBLIC
                .requestMatchers("/auth/**").permitAll()

                // 👤 USER + ADMIN
                .requestMatchers("/applications").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/applications/my").hasAnyRole("USER", "ADMIN")

                // 🔥 ADMIN ONLY
                .requestMatchers("/applications/*/internal/approve").hasRole("ADMIN")
                .requestMatchers("/applications/*/internal/reject").hasRole("ADMIN")

                // 🔒 everything else
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}