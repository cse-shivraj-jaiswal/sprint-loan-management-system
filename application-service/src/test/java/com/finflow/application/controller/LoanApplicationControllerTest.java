package com.finflow.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.application.model.ApplicationStatus;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.LoanType;
import com.finflow.application.security.JwtUtil;
import com.finflow.application.service.LoanApplicationService;
import com.finflow.application.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties = { "spring.cloud.config.enabled=false", "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration" }, value = LoanApplicationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple testing
public class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanApplicationService loanApplicationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanApplication testApp;

    @BeforeEach
    void setUp() {
        testApp = new LoanApplication();
        testApp.setId(1L);
        testApp.setUserId(1L);
        testApp.setLoanType(LoanType.PERSONAL);
        testApp.setStatus(ApplicationStatus.DRAFT);
        testApp.setAmount(50000.0);
    }

    @Test
    void create_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authClient.getUserIdByEmail("test@example.com")).thenReturn(1L);
        when(loanApplicationService.create(anyLong(), any())).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(post("/applications")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testApp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void submit_ValidDraft_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        testApp.setStatus(ApplicationStatus.SUBMITTED);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authClient.getUserIdByEmail("test@example.com")).thenReturn(1L);
        when(loanApplicationService.submit(1L, 1L)).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(post("/applications/1/submit")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void getMyApplications_ValidRequest_ShouldReturnList() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(authClient.getUserIdByEmail("test@example.com")).thenReturn(1L);
        when(loanApplicationService.getByUser(1L)).thenReturn(List.of(testApp));

        // Act & Assert
        mockMvc.perform(get("/applications/my")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    void approveInternal_AdminRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer admin-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(loanApplicationService.approve(1L)).thenReturn(Map.of("message", "Application approved"));

        // Act & Assert
        mockMvc.perform(post("/applications/1/internal/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Application approved"));
    }
}
