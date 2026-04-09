package com.finflow.document.service;

import com.finflow.document.client.ApplicationClient;
import com.finflow.document.dto.LoanApplicationDTO;
import com.finflow.document.model.Document;
import com.finflow.document.model.DocumentStatus;
import com.finflow.document.model.LoanType;
import com.finflow.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private ApplicationClient applicationClient;

    @InjectMocks
    private DocumentService service;

    @TempDir
    Path tempDir;

    private LoanApplicationDTO testApp;
    private Document testDoc;
    private String token = "test-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

        testApp = new LoanApplicationDTO();
        testApp.setId(1L);
        testApp.setUserId(1L);
        testApp.setStatus("SUBMITTED");
        testApp.setLoanType(LoanType.PERSONAL.name());

        testDoc = new Document();
        testDoc.setId(10L);
        testApp.setUserId(1L);
        testDoc.setUserId(1L);
        testDoc.setApplicationId(1L);
        testDoc.setDocumentType("PAN_CARD");
        testDoc.setStatus(DocumentStatus.UPLOADED);
    }

    @Test
    void upload_ValidRequest_ShouldSucceed() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApp);
        when(repository.existsByApplicationIdAndDocumentType(anyLong(), anyString())).thenReturn(false);
        when(repository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Document result = service.upload(token, 1L, 1L, "PAN_CARD", file);

        // Assert
        assertNotNull(result);
        assertEquals(DocumentStatus.UPLOADED, result.getStatus());
        assertEquals("PAN_CARD", result.getDocumentType());
        verify(repository).save(any(Document.class));
    }

    @Test
    void upload_UnauthorizedUser_ShouldThrowException() throws Exception {
        // Arrange
        testApp.setUserId(2L); // Different user
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApp);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.upload(token, 1L, 1L, "PAN_CARD", file));
    }

    @Test
    void verify_SubmittedApplication_ShouldSucceed() {
        // Arrange
        when(repository.findById(10L)).thenReturn(Optional.of(testDoc));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApp);
        when(repository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Document result = service.verify(10L, token);

        // Assert
        assertEquals(DocumentStatus.VERIFIED, result.getStatus());
        verify(repository).save(testDoc);
    }

    @Test
    void verify_DraftApplication_ShouldThrowException() {
        // Arrange
        testApp.setStatus("DRAFT");
        when(repository.findById(10L)).thenReturn(Optional.of(testDoc));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApp);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.verify(10L, token));
    }

    @Test
    void reject_ValidRequest_ShouldSucceed() {
        // Arrange
        when(repository.findById(10L)).thenReturn(Optional.of(testDoc));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApp);
        when(repository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Document result = service.reject(10L, "Illegible copy", token);

        // Assert
        assertEquals(DocumentStatus.REJECTED, result.getStatus());
        assertEquals("Illegible copy", result.getRemarks());
        verify(repository).save(testDoc);
    }
}