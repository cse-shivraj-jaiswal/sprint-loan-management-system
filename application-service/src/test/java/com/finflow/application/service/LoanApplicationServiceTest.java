package com.finflow.application.service;

import com.finflow.application.client.DocumentClient;
import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;
import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.ApplicationStatus;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.LoanType;
import com.finflow.application.model.OccupationType;
import com.finflow.application.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @Mock
    private LoanValidationService validationService;

    @Mock
    private DocumentClient documentClient;

    @InjectMocks
    private LoanApplicationService service;

    private LoanApplication testApp;
    private CreateApplicationRequest createRequest;

    @BeforeEach
    void setUp() {
        testApp = new LoanApplication();
        testApp.setId(1L);
        testApp.setUserId(1L);
        testApp.setLoanType(LoanType.PERSONAL);
        testApp.setStatus(ApplicationStatus.DRAFT);
        testApp.setAmount(50000.0);

        createRequest = new CreateApplicationRequest();
        createRequest.setLoanType(LoanType.PERSONAL);
        createRequest.setAmount(50000.0);
        createRequest.setTenure(12);
        createRequest.setIncome(60000.0);
        createRequest.setOccupation(OccupationType.SALARIED);
        createRequest.setAge(25);
    }

    // ===============================
    // ✅ CREATE TESTS
    // ===============================

    @Test
    void create_NewApplication_ShouldSucceed() {
        // Arrange
        when(repository.findByUserIdAndLoanType(anyLong(), any())).thenReturn(Collections.emptyList());
        when(repository.save(any(LoanApplication.class))).thenReturn(testApp);

        // Act
        LoanApplication result = service.create(1L, createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        verify(validationService).validate(any());
        verify(repository).save(any());
    }

    @Test
    void create_ExistingActiveApplication_ShouldThrowException() {
        // Arrange
        when(repository.findByUserIdAndLoanType(eq(1L), eq(LoanType.PERSONAL)))
                .thenReturn(List.of(testApp));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> service.create(1L, createRequest));
    }

    // ===============================
    // ✅ SUBMIT TESTS
    // ===============================

    @Test
    void submit_ValidDraft_ShouldSucceed() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));
        when(documentClient.validateDocuments(anyLong(), any())).thenReturn(true);
        when(repository.save(any())).thenReturn(testApp);

        // Act
        LoanApplication result = service.submit(1L, 1L);

        // Assert
        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        verify(repository).save(result);
    }

    @Test
    void submit_MissingDocuments_ShouldThrowException() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));
        when(documentClient.validateDocuments(anyLong(), any())).thenReturn(false);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.submit(1L, 1L));
        assertEquals("Please upload the documents first.", ex.getMessage());
    }

    @Test
    void submit_AlreadySubmitted_ShouldThrowException() {
        // Arrange
        testApp.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> service.submit(1L, 1L));
    }

    // ===============================
    // ✅ APPROVE/REJECT TESTS
    // ===============================

    @Test
    void approve_SubmittedApplication_ShouldSucceed() {
        // Arrange
        testApp.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        // Act
        service.approve(1L);

        // Assert
        assertEquals(ApplicationStatus.APPROVED, testApp.getStatus());
        verify(repository).save(testApp);
    }

    @Test
    void approve_DraftApplication_ShouldThrowException() {
        // Arrange
        testApp.setStatus(ApplicationStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> service.approve(1L));
    }

    @Test
    void reject_SubmittedApplication_ShouldSucceed() {
        // Arrange
        testApp.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(testApp));

        // Act
        service.reject(1L);

        // Assert
        assertEquals(ApplicationStatus.REJECTED, testApp.getStatus());
        verify(repository).save(testApp);
    }
}