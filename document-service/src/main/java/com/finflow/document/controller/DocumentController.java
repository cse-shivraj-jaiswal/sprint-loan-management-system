package com.finflow.document.controller;

import com.finflow.document.client.AuthClient;
import com.finflow.document.enums.*;
import com.finflow.document.model.Document;
import com.finflow.document.model.LoanType;
import com.finflow.document.security.JwtUtil;
import com.finflow.document.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Document APIs", description = "Upload and manage documents")
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;
    private final JwtUtil jwtUtil;
    private final AuthClient authClient;

    public DocumentController(DocumentService service,
                              JwtUtil jwtUtil,
                              AuthClient authClient) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.authClient = authClient;
    }

    // ===============================
    // 🔐 COMMON METHOD (JWT handling)
    // ===============================
    private Long extractUserId(String header) {
        String token = header.substring(7);
        String email = jwtUtil.extractEmail(token);
        return authClient.getUserIdByEmail(header, email);
    }

    // ===============================
    // 🚫 BLOCK ADMIN UPLOAD
    // ===============================
    private void blockAdminUpload(String header) {
        String token = header.substring(7);
        String role = jwtUtil.extractRole(token);
        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Documents should be uploaded by loan applicants");
        }
    }

    // ===============================
    // 🏠 HOME LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Home Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/home", consumes = "multipart/form-data")
    public Document uploadHome(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam HomeLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 🎓 EDUCATION LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Education Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/education", consumes = "multipart/form-data")
    public Document uploadEducation(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam EducationLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 💼 BUSINESS LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Business Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/business", consumes = "multipart/form-data")
    public Document uploadBusiness(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam BusinessLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 🚗 VEHICLE LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Vehicle Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/vehicle", consumes = "multipart/form-data")
    public Document uploadVehicle(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam VehicleLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 💍 MARRIAGE LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Marriage Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/marriage", consumes = "multipart/form-data")
    public Document uploadMarriage(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam MarriageLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 👤 PERSONAL LOAN UPLOAD
    // ===============================
    @Operation(summary = "Upload Personal Loan Document")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload/personal", consumes = "multipart/form-data")
    public Document uploadPersonal(
            @RequestHeader("Authorization") String header,
            @RequestParam Long applicationId,
            @RequestParam PersonalLoanDocument documentType,
            @RequestParam MultipartFile file) throws Exception {

        blockAdminUpload(header);
        Long userId = extractUserId(header);

        return service.upload(header, userId, applicationId, documentType.name(), file);
    }

    // ===============================
    // 🔄 Replace Document
    // ===============================
    @Operation(summary = "Replace document")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Document replace(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id,
            @RequestParam MultipartFile file) throws Exception {

        Long userId = extractUserId(header);

        return service.replace(header, userId, id, file);
    }

    // ===============================
    // 📄 Get Documents by Application
    // ===============================
    @Operation(summary = "Get documents by application")
    @GetMapping("/application/{applicationId}")
    public List<Document> getByApplication(@PathVariable Long applicationId) {
        return service.getByApplication(applicationId);
    }

    // ===============================
    // ✅ Validate Documents
    // ===============================
    @Operation(summary = "Validate documents for loan")
    @GetMapping("/validate/{applicationId}/{loanType}")
    public boolean validateDocuments(
            @PathVariable Long applicationId,
            @PathVariable LoanType loanType) {

        return service.areDocumentsComplete(applicationId, loanType);
    }

    // ===============================
    // ✅ Admin Verify Document (INTERNAL ONLY)
    // ===============================
    @Hidden
    @PutMapping("/internal/{id}/verify")
    public Document verify(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {
        return service.verify(id, header);
    }

    // ===============================
    // ❌ Reject Document (INTERNAL ONLY)
    // ===============================
    @Hidden
    @PutMapping("/internal/{id}/reject")
    public Document reject(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id,
            @RequestParam String remarks) {

        return service.reject(id, remarks, header);
    }
}