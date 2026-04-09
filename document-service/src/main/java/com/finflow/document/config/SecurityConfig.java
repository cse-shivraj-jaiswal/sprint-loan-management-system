package com.finflow.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.finflow.document.security.CustomAccessDeniedHandler;
import com.finflow.document.security.CustomAuthEntryPoint;
import com.finflow.document.security.JwtFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthEntryPoint authEntryPoint;

    public SecurityConfig(JwtFilter jwtFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthEntryPoint authEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            // 🔥 Exception Handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

            .authorizeHttpRequests(auth -> auth

                // 🔓 Swagger
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/*/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // 🔥 VALIDATE → USER + ADMIN
                .requestMatchers("/documents/validate/**").hasAnyRole("USER", "ADMIN")

                // 📄 Upload / Replace → USER ONLY (admins cannot upload on behalf of applicants)
                .requestMatchers("/documents/upload/**").hasRole("USER")
                .requestMatchers("/documents/{id}").hasRole("USER")
                .requestMatchers("/documents/application/**").hasAnyRole("USER", "ADMIN")

                // 🔥 ADMIN ONLY (MUST COME BEFORE GENERIC MATCH)
                .requestMatchers("/documents/internal/**").hasRole("ADMIN")

                // ⚠️ GENERIC (keep LAST)
                .requestMatchers("/documents/**").hasAnyRole("USER", "ADMIN")

                // 🔒 Everything else
                .anyRequest().authenticated()
            )

            // 🔐 JWT Filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}