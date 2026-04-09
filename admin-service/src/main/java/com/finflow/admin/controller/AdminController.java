package com.finflow.admin.controller;

import org.springframework.web.bind.annotation.*;

import com.finflow.admin.client.AuthClient;
import com.finflow.admin.service.AdminService;
import com.finflow.admin.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Admin APIs", description = "Admin operations for loan system")
@RestController
@RequestMapping
public class AdminController {

    private final AdminService service;
    private final JwtUtil jwtUtil;
    private final AuthClient authClient;

    public AdminController(AdminService service, JwtUtil jwtUtil, AuthClient authClient) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.authClient = authClient;
    }

    // ===============================
    // 🔐 COMMON ADMIN CHECK
    // ===============================
    private void validateAdmin(String header) {
        String token = header.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Admin only");
        }
    }

    // ===============================
    // 🔐 EXTRACT ADMIN ID FROM TOKEN
    // ===============================
    private Long getAdminId(String header) {
        String token = header.substring(7);
        String email = jwtUtil.extractEmail(token);
        return authClient.getUserIdByEmail(header, email);
    }

    // ===============================
    // 🔍 VIEW ALL APPLICATIONS
    // ===============================
    @Operation(summary = "Get all applications (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/applications")
    public Object getApplications(
            @RequestHeader("Authorization") String header){

        validateAdmin(header);

        return service.getApplications(header); // ✅ FIXED
    }

    // ===============================
    // 🔍 VIEW SINGLE APPLICATION
    // ===============================
    @Operation(summary = "Get application by ID (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/applications/{appId}")
    public Object getApplicationById(
            @RequestHeader("Authorization") String header,
            @PathVariable Long appId) {

        validateAdmin(header);

        return service.getApplicationById(appId, header);
    }

    // ===============================
    // 📄 VIEW DOCUMENTS
    // ===============================
    @Operation(summary = "Get documents by application ID (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/documents/{appId}")
    public Object getDocuments(
            @RequestHeader("Authorization") String header,
            @PathVariable Long appId) {

        validateAdmin(header);

        return service.getDocuments(appId, header);
    }

    // ===============================
    // ✅ VERIFY DOCUMENT
    // ===============================
    @Operation(summary = "Verify document (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/documents/{docId}/verify")
    public Object verifyDocument(
            @RequestHeader("Authorization") String header,
            @PathVariable Long docId) {

        validateAdmin(header);

        return service.verifyDocument(docId, header);
    }

    // ===============================
    // ❌ REJECT DOCUMENT
    // ===============================
    @Operation(summary = "Reject document with remarks (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/documents/{docId}/reject")
    public Object rejectDocument(
            @RequestHeader("Authorization") String header,
            @PathVariable Long docId,
            @RequestParam String remarks) {

        validateAdmin(header);

        return service.rejectDocument(docId, remarks, header);
    }

    // ===============================
    // ✅ APPROVE APPLICATION
    // ===============================
    @Operation(summary = "Approve loan application (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/applications/{appId}/approve")
    public Object approveApplication(
            @RequestHeader("Authorization") String header,
            @PathVariable Long appId) {

        validateAdmin(header);

        Long adminId = getAdminId(header);

        return service.approve(appId, adminId, header);
    }

    // ===============================
    // ❌ REJECT APPLICATION
    // ===============================
    @Operation(summary = "Reject loan application (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/applications/{appId}/reject")
    public Object rejectApplication(
            @RequestHeader("Authorization") String header,
            @PathVariable Long appId,
            @RequestParam String remarks) {

        validateAdmin(header);

        Long adminId = getAdminId(header);

        return service.reject(appId, adminId, remarks, header);
    }

    // ===============================
    // 📊 DASHBOARD
    // ===============================
    @Operation(summary = "Get admin dashboard report (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/dashboard")
    public Object dashboard(
            @RequestHeader("Authorization") String header) {

        validateAdmin(header);

        Long adminId = getAdminId(header); // Extract Auth User ID

        return service.generateReport(adminId, header);
    }
}