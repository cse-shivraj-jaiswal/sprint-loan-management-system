package com.finflow.document.controller;

import com.finflow.document.model.Document;
import com.finflow.document.model.DocumentStatus;
import com.finflow.document.security.JwtUtil;
import com.finflow.document.client.AuthClient;
import com.finflow.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties = { "spring.cloud.config.enabled=false", "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration" }, value = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple testing
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    private Document testDoc;

    @BeforeEach
    void setUp() {
        testDoc = new Document();
        testDoc.setId(1L);
        testDoc.setUserId(1L);
        testDoc.setApplicationId(1L);
        testDoc.setDocumentType("PAN_CARD");
        testDoc.setStatus(DocumentStatus.UPLOADED);
        testDoc.setUploadedAt(LocalDateTime.now());
    }

    @Test
    void uploadHome_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(1L);
        when(documentService.upload(anyString(), anyLong(), anyLong(), anyString(), any())).thenReturn(testDoc);

        // Act & Assert
        mockMvc.perform(multipart("/documents/upload/home")
                .file(file)
                .param("applicationId", "1")
                .param("documentType", "PAN") // Matches HomeLoanDocument enum entry
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("PAN_CARD"));
    }

    @Test
    void verify_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String token = "Bearer test-token";
        testDoc.setStatus(DocumentStatus.VERIFIED);
        when(documentService.verify(anyLong(), anyString())).thenReturn(testDoc);

        // Act & Assert
        mockMvc.perform(put("/documents/internal/1/verify")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void getByApplication_ValidRequest_ShouldReturnList() throws Exception {
        // Arrange
        when(documentService.getByApplication(anyLong())).thenReturn(List.of(testDoc));

        // Act & Assert
        mockMvc.perform(get("/documents/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].documentType").value("PAN_CARD"));
    }
}
