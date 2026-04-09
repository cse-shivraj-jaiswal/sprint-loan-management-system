package com.finflow.document.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ===============================
        // 🔥 BYPASS PUBLIC + SWAGGER
        // ===============================
        if (path.contains("/v3/api-docs") ||
            path.contains("/swagger-ui") ||
            path.contains("/swagger-resources") ||
            path.contains("/webjars")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ===============================
        // 🔐 CHECK HEADER
        // ===============================
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header missing");
            return;
        }

        String token = header.substring(7);

        // ===============================
        // 🔐 VALIDATE TOKEN
        // ===============================
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        // ===============================
        // 🔥 SET SECURITY CONTEXT
        // ===============================
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token); // USER / ADMIN

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(authority)
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // ===============================
        // ✅ CONTINUE FILTER CHAIN
        // ===============================
        filterChain.doFilter(request, response);
    }
}