package com.finflow.auth.service;

import com.finflow.auth.dto.*;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 🔥 SIGNUP
    public AuthResponse signup(SignupRequest request) {

        String email = request.getEmail().trim();

        // Check if user already exists
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);

        // 🔐 Encrypt password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);
        user.setTermsAccepted(false);

        userRepository.save(user);

        return new AuthResponse(null, "User registered successfully");
    }

    // 🔥 LOGIN
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔐 SECURE PASSWORD CHECK
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isTermsAccepted()) {
            throw new RuntimeException("Please accept terms first");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(token, "Login successful");
    }

    // 🔥 ACCEPT TERMS
    public String acceptTerms(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTermsAccepted(true);

        userRepository.save(user);

        return "Terms accepted successfully";
    }
}