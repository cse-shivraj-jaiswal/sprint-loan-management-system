package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.model.Report;
import com.finflow.admin.repository.DecisionRepository;
import com.finflow.admin.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private ApplicationClient applicationClient;

    @Mock
    private DocumentClient documentClient;

    @Mock
    private DecisionRepository decisionRepo;

    @Mock
    private ReportRepository reportRepo;

    @InjectMocks
    private AdminService adminService;

    private Map<String, Object> testApp;
    private String token = "test-token";

    @BeforeEach
    void setUp() {
        testApp = new HashMap<>();
        testApp.put("id", 1L);
        testApp.put("userId", 100L);
        testApp.put("status", "SUBMITTED");
        testApp.put("loanType", "PERSONAL");
    }

    @Test
    void getApplications_ShouldFilterOutDrafts() {
        // Arrange
        Map<String, Object> draftApp = new HashMap<>();
        draftApp.put("status", "DRAFT");

        when(applicationClient.getApplications(anyString()))
                .thenReturn(Arrays.asList(testApp, draftApp));

        // Act
        List<Map<String, Object>> result = adminService.getApplications(token);

        // Assert
        assertEquals(1, result.size());
        assertEquals("SUBMITTED", result.get(0).get("status"));
    }

    @Test
    void approve_ValidApplication_ShouldSucceed() {
        // Arrange
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApp);
        
        Map<String, Object> verifiedDoc = new HashMap<>();
        verifiedDoc.put("status", "VERIFIED");
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(List.of(verifiedDoc));
        when(documentClient.validateDocuments(anyLong(), anyString(), anyString())).thenReturn(true);
        when(applicationClient.approve(anyLong(), anyString())).thenReturn(Map.of("message", "Application approved"));

        // Act
        Map<String, String> response = adminService.approve(1L, 500L, token);

        // Assert
        assertEquals("Application approved", response.get("message"));
        verify(decisionRepo).save(any());
    }

    @Test
    void approve_UnverifiedDocuments_ShouldThrowException() {
        // Arrange
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApp);
        
        Map<String, Object> uploadedDoc = new HashMap<>();
        uploadedDoc.put("status", "UPLOADED"); // Not verified
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(List.of(uploadedDoc));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.approve(1L, 500L, token));
    }

    @Test
    void reject_ValidRequest_ShouldSucceed() {
        // Arrange
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApp);
        when(applicationClient.reject(anyLong(), anyString())).thenReturn(Map.of("message", "Application rejected"));

        // Act
        Map<String, String> response = adminService.reject(1L, 500L, "Poor credit score", token);

        // Assert
        assertEquals("Application rejected", response.get("message"));
        verify(decisionRepo).save(any());
    }

    @Test
    void generateReport_ShouldCalculateCorrectly() {
        // Arrange
        Map<String, Object> app1 = new HashMap<>(testApp);
        app1.put("status", "APPROVED");

        Map<String, Object> app2 = new HashMap<>(testApp);
        app2.put("status", "REJECTED");

        Map<String, Object> app3 = new HashMap<>(testApp);
        app3.put("status", "SUBMITTED");

        when(applicationClient.getApplications(anyString())).thenReturn(Arrays.asList(app1, app2, app3));
        when(reportRepo.findById(anyLong())).thenReturn(Optional.empty());
        when(reportRepo.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Report report = adminService.generateReport(500L, token);

        // Assert
        assertEquals(3, report.getTotalApplications());
        assertEquals(1, report.getApprovedCount());
        assertEquals(1, report.getRejectedCount());
        assertEquals(1, report.getPendingCount());
    }
}