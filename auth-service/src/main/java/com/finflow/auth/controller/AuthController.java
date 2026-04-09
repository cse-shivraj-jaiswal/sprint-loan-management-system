package com.finflow.auth.controller;

import com.finflow.auth.dto.*;
import com.finflow.auth.service.AuthService;
import com.finflow.auth.security.JwtUtil;
import com.finflow.auth.model.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "APIs for authentication and user management")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ===============================
    // ✅ SIGNUP
    // ===============================
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    // ===============================
    // ✅ LOGIN
    // ===============================
    @Operation(summary = "Login user and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // ===============================
    // ✅ ACCEPT TERMS
    // ===============================
    @Operation(summary = "Accept terms and conditions")
    @PostMapping("/accept-terms/{userId}")
    public String acceptTerms(@PathVariable Long userId) {
        return authService.acceptTerms(userId);
    }

    // ===============================
    // 🔐 TEST JWT TOKEN
    // ===============================
    @Operation(summary = "Validate JWT token and extract details")
    @SecurityRequirement(name = "bearerAuth")   // 🔥 IMPORTANT
    @GetMapping("/test-token")
    public String test(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        String email = jwtUtil.extractEmail(token);
        Role role = Role.valueOf(jwtUtil.extractRole(token));

        return "Email: " + email + ", Role: " + role;
    }

    // ===============================
    // 🔍 GET USER ID BY EMAIL
    // ===============================
    @Operation(summary = "Get user ID using email")
    @SecurityRequirement(name = "bearerAuth")   // 🔥 SECURED
    @GetMapping("/user/email/{email}")
    public Long getUserIdByEmail(@PathVariable String email) {
        return authService.getUserIdByEmail(email);
    }
}