package com.finflow.auth.service;

import com.finflow.auth.dto.*;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 🔥 SIGNUP
    public AuthResponse signup(SignupRequest request) {

        String email = request.getEmail().trim();

        // 🔥 CHECK: USER ALREADY EXISTS
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
        	return new AuthResponse(
        	        null,                        // token
        	        existingUser.get().getId(),  // userId 🔥
        	        "User already registered, you can directly login"
        	);
        }

        // ✅ CREATE NEW USER
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);

        // 🔐 Encrypt password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);
        user.setTermsAccepted(false);

        // 🔥 SAVE AND CAPTURE USER
        User savedUser = userRepository.save(user);

        // 🐇 PUBLISH EVENT TO RABBITMQ
        try {
            UserEvent userEvent = UserEvent.builder()
                    .eventType("USER_CREATED")
                    .email(savedUser.getEmail())
                    .name(savedUser.getName())
                    .message("Welcome to FinFlow! Your account has been created successfully.")
                    .build();

            rabbitTemplate.convertAndSend(
                    com.finflow.auth.config.RabbitMQConfig.EXCHANGE_LOAN,
                    com.finflow.auth.config.RabbitMQConfig.ROUTING_KEY_AUTH,
                    userEvent
            );
            System.out.println("✅ Published USER_CREATED event for: " + savedUser.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to publish USER_CREATED event: " + e.getMessage());
        }

        // ✅ RETURN USER ID
        return new AuthResponse(
                null,                    // token
                savedUser.getId(),       // userId 🔥
                "User registered successfully"
        );
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
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        System.out.println("===== AUTH SERVICE TOKEN =====");
        System.out.println(token);
        System.out.println("==============================");
        
        return new AuthResponse(token, null, "Login successful");
    }

    // 🔥 ACCEPT TERMS
    public String acceptTerms(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTermsAccepted(true);

        userRepository.save(user);

        return "Terms accepted successfully";
    }
    
    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getId();
    }
}