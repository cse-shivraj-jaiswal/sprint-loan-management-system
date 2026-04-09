package com.finflow.document.service;

import com.finflow.document.client.ApplicationClient;
import com.finflow.document.dto.LoanApplicationDTO;
import com.finflow.document.model.*;
import com.finflow.document.repository.DocumentRepository;
import com.finflow.document.util.LoanDocumentRequirements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final ApplicationClient applicationClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public DocumentService(DocumentRepository repository,
                           ApplicationClient applicationClient,
                           org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.applicationClient = applicationClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    private void publishDocumentEvent(Document doc, String eventType, String message) {
        try {
            com.finflow.document.dto.DocumentEvent event = com.finflow.document.dto.DocumentEvent.builder()
                    .eventType(eventType)
                    .userId(doc.getUserId().toString())
                    .applicationId(doc.getApplicationId().toString())
                    .documentType(doc.getDocumentType())
                    .status(doc.getStatus().name())
                    .remarks(doc.getRemarks())
                    .email("user" + doc.getUserId() + "@example.com") // Mock email
                    .name("User " + doc.getUserId())
                    .message(message)
                    .build();

            rabbitTemplate.convertAndSend(
                    com.finflow.document.config.RabbitMQConfig.EXCHANGE_LOAN,
                    com.finflow.document.config.RabbitMQConfig.ROUTING_KEY_DOCUMENT,
                    event
            );
            System.out.println("✅ Published " + eventType + " event for document: " + doc.getId());
        } catch (Exception e) {
            System.err.println("❌ Failed to publish " + eventType + " event: " + e.getMessage());
        }
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    // =========================
    // ✅ UPLOAD DOCUMENT
    // =========================
    public Document upload(String token,
                           Long userId,
                           Long applicationId,
                           String documentType,
                           MultipartFile file) throws Exception {

        // 🔐 OWNERSHIP CHECK
        LoanApplicationDTO app = applicationClient.getById(token, applicationId);
        if (app == null || !app.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You do not own this application");
        }

        // 🔴 VALIDATION
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is missing");
        }

        // 🔥 NEW: DUPLICATE CHECK
        boolean exists = repository.existsByApplicationIdAndDocumentType(applicationId, documentType);
        if (exists) {
            throw new RuntimeException("Document already uploaded");
        }

        // 📁 SAVE FILE
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        // 💾 SAVE DB
        Document doc = new Document();
        doc.setUserId(userId);
        doc.setApplicationId(applicationId);
        doc.setDocumentType(documentType); // ✅ STRING
        doc.setFileUrl(path.toString());
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setUploadedAt(LocalDateTime.now());

        Document saved = repository.save(doc);
        publishDocumentEvent(saved, "DOCUMENT_UPLOADED", "We've received your " + documentType + ". It is now under review.");
        return saved;
    }

    // =========================
    // 🔄 REPLACE DOCUMENT
    // =========================
    public Document replace(String token,
                            Long userId,
                            Long id,
                            MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is missing");
        }

        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 🔐 OWNERSHIP CHECK (Local + Cross-service)
        if (!doc.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        LoanApplicationDTO app = applicationClient.getById(token, doc.getApplicationId());
        if (app == null || !app.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Application ownership mismatch");
        }

        // 🗑️ DELETE OLD FILE
        if (doc.getFileUrl() != null) {
            Files.deleteIfExists(Paths.get(doc.getFileUrl()));
        }

        // 📁 SAVE NEW FILE
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        // 🔁 UPDATE SAME OBJECT
        doc.setFileUrl(path.toString());
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setUpdatedAt(LocalDateTime.now());

        return repository.save(doc);
    }

    // =========================
    // 📄 GET DOCUMENTS
    // =========================
    public List<Document> getByApplication(Long applicationId) {
        return repository.findByApplicationId(applicationId);
    }

    // =========================
    // 🔥 VALIDATE DOCUMENTS (FIXED)
    // =========================
    public boolean areDocumentsComplete(Long applicationId, LoanType loanType) {

        List<Document> uploadedDocs = repository.findByApplicationId(applicationId);

        // 🔥 STRING SET (FIXED)
        Set<String> uploadedTypes = uploadedDocs.stream()
                .filter(doc -> doc.getStatus() == DocumentStatus.UPLOADED
                        || doc.getStatus() == DocumentStatus.VERIFIED)
                .map(Document::getDocumentType)
                .collect(Collectors.toSet());

        // 🔥 REQUIRED DOCS → STRING
        Set<String> requiredDocs = LoanDocumentRequirements
                .getRequiredDocs(loanType)
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return uploadedTypes.containsAll(requiredDocs);
    }

    // =========================
    // ✅ VERIFY DOCUMENT
    // =========================
    public Document verify(Long id, String token) {

        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Long applicationId = doc.getApplicationId();

        // 🔥 CALL APPLICATION SERVICE
        LoanApplicationDTO app = applicationClient.getById(token, applicationId);

        if ("DRAFT".equals(app.getStatus())) {
            throw new RuntimeException("Cannot verify documents before submission");
        }

        doc.setStatus(DocumentStatus.VERIFIED);

        Document saved = repository.save(doc);
        publishDocumentEvent(saved, "DOCUMENT_VERIFIED", "Your " + saved.getDocumentType() + " has been verified successfully.");
        return saved;
    }

    // =========================
    // ❌ REJECT DOCUMENT
    // =========================
    public Document reject(Long id, String remarks, String token) {

        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Long applicationId = doc.getApplicationId();

        // 🔥 validation
        LoanApplicationDTO app = applicationClient.getById(token, applicationId);

        if ("DRAFT".equals(app.getStatus())) {
            throw new RuntimeException("Cannot reject before submission");
        }

        doc.setStatus(DocumentStatus.REJECTED);
        doc.setRemarks(remarks);

        Document saved = repository.save(doc);
        publishDocumentEvent(saved, "DOCUMENT_REJECTED", "⚠️ Action Required: Your " + saved.getDocumentType() + " was rejected. Reason: " + remarks);
        return saved;
    }
    
}