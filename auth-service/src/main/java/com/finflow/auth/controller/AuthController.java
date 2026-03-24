package com.finflow.auth.controller;

import com.finflow.auth.dto.*;
import com.finflow.auth.service.AuthService;
import com.finflow.auth.security.JwtUtil;
import com.finflow.auth.model.Role;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;   // ✅ ADD THIS

    // ✅ Constructor Injection (Best Practice)
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/accept-terms/{userId}")
    public String acceptTerms(@PathVariable Long userId) {
        return authService.acceptTerms(userId);
    }

    // 🔥 TEST JWT
    @GetMapping("/test-token")
    public String test(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        String email = jwtUtil.extractEmail(token);

        Role role = jwtUtil.extractRole(token); // ✅ FIXED

        return "Email: " + email + ", Role: " + role;
    }
}