package com.finflow.admin.controller;

import com.finflow.admin.service.AdminService;
import com.finflow.admin.security.JwtUtil;
import com.finflow.admin.client.AuthClient;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService service;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    private final String token = "Bearer valid-token";

    @Test
    void testAuthorizedAccess() throws Exception {
        Mockito.when(jwtUtil.extractRole(Mockito.anyString())).thenReturn("ADMIN");
        Mockito.when(jwtUtil.extractEmail(Mockito.anyString())).thenReturn("admin@test.com");
        Mockito.when(authClient.getUserIdByEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(500L);
        Mockito.when(service.generateReport(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(null);

        mockMvc.perform(get("/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthorizedAccess_NoToken() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isBadRequest());
    }
}