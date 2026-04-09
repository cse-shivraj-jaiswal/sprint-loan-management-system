package com.finflow.auth.service;

import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setTermsAccepted(true);

        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setName("Test User");
        signupRequest.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
    }

    // ===============================
    // 🔥 SIGNUP TESTS
    // ===============================

    @Test
    void signup_NewUser_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.signup(signupRequest);

        // Assert
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertNull(response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_ExistingUser_ShouldReturnMessage() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.signup(signupRequest);

        // Assert
        assertEquals("User already registered, you can directly login", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository, never()).save(any());
    }

    // ===============================
    // 🔥 LOGIN TESTS
    // ===============================

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertEquals("Login successful", response.getMessage());
        assertEquals("test-token", response.getToken());
    }

    @Test
    void login_InvalidPassword_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_TermsNotAccepted_ShouldThrowException() {
        // Arrange
        testUser.setTermsAccepted(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    // ===============================
    // 🔥 TERMS TESTS
    // ===============================

    @Test
    void acceptTerms_ValidUser_ShouldSucceed() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        String result = authService.acceptTerms(1L);

        // Assert
        assertEquals("Terms accepted successfully", result);
        assertTrue(testUser.isTermsAccepted());
        verify(userRepository).save(testUser);
    }
}