package com.finflow.admin.controller;

import com.finflow.admin.model.Report;
import com.finflow.admin.service.AdminService;
import com.finflow.admin.security.JwtUtil;
import com.finflow.admin.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties = { "spring.cloud.config.enabled=false", "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration" }, value = AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple testing
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    private Report testReport;
    private Map<String, Object> testApp;

    @BeforeEach
    void setUp() {
        testReport = new Report();
        testReport.setId(500L);
        testReport.setTotalApplications(10);
        testReport.setApprovedCount(5);
        testReport.setPendingCount(5);
        testReport.setGeneratedAt(LocalDateTime.now());

        testApp = new HashMap<>();
        testApp.put("id", 1L);
        testApp.put("status", "SUBMITTED");
    }

    @Test
    void getApplications_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(adminService.getApplications(anyString())).thenReturn(Collections.singletonList(testApp));

        // Act & Assert
        mockMvc.perform(get("/applications")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    void approve_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(jwtUtil.extractEmail(anyString())).thenReturn("admin@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(500L);
        when(adminService.approve(anyLong(), anyLong(), anyString())).thenReturn(Collections.singletonMap("message", "Approved"));

        // Act & Assert
        mockMvc.perform(post("/applications/1/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approved"));
    }

    @Test
    void reject_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(jwtUtil.extractEmail(anyString())).thenReturn("admin@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(500L);
        when(adminService.reject(anyLong(), anyLong(), anyString(), anyString())).thenReturn(Collections.singletonMap("message", "Rejected"));

        // Act & Assert
        mockMvc.perform(post("/applications/1/reject")
                .param("remarks", "Missing docs")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rejected"));
    }

    @Test
    void getDashboard_ValidRequest_ShouldReturnReport() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(jwtUtil.extractEmail(anyString())).thenReturn("admin@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(500L);
        when(adminService.generateReport(anyLong(), anyString())).thenReturn(testReport);

        // Act & Assert
        mockMvc.perform(get("/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(10))
                .andExpect(jsonPath("$.approvedCount").value(5));
    }
}
