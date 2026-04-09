package com.finflow.application.controller;

import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.security.JwtUtil;
import com.finflow.application.service.LoanApplicationService;
import com.finflow.application.client.AuthClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Loan Application APIs", description = "Manage loan applications")
@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService service;
    private final JwtUtil jwtUtil;
    private final AuthClient authClient;

    public LoanApplicationController(LoanApplicationService service,
            JwtUtil jwtUtil,
            AuthClient authClient) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.authClient = authClient;
    }

    // ===============================
    // 🔐 COMMON USER ID FETCH
    // ===============================
    private Long getUserId(String header) {

        String token = header.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        String email = jwtUtil.extractEmail(token);
        return authClient.getUserIdByEmail(email);
    }

    // ===============================
    // 🔐 ADMIN CHECK
    // ===============================
    private void validateAdmin(String header) {
        String token = header.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Admin only");
        }
    }

    // ===============================
    // 🔐 USER CHECK
    // ===============================
    private void validateUser(String header) {
        String token = header.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"USER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: User only");
        }
    }

    // ===============================
    // ✅ CREATE
    // ===============================
    @Operation(summary = "Create loan application")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public LoanApplication create(
            @RequestHeader("Authorization") String header,
            @RequestBody CreateApplicationRequest request) {

        validateUser(header);
        Long userId = getUserId(header);
        return service.create(userId, request);
    }

    // ===============================
    // ✅ UPDATE (ONLY USER)
    // ===============================
    @Operation(summary = "Update loan application")
    @PutMapping("/{id}")
    public LoanApplication update(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id,
            @RequestBody UpdateApplicationRequest request) {

        validateUser(header);
        Long userId = getUserId(header);
        return service.update(userId, id, request);
    }

    // ===============================
    // ✅ SUBMIT
    // ===============================
    @Operation(summary = "Submit loan application")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/submit")
    public LoanApplication submit(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {

        Long userId = getUserId(header);
        return service.submit(userId, id);
    }

    // ===============================
    // ✅ GET MY APPLICATIONS
    // ===============================
    @Operation(summary = "Get my applications")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public List<LoanApplication> getMyApplications(
            @RequestHeader("Authorization") String header) {

        validateUser(header);
        Long userId = getUserId(header);
        return service.getByUser(userId);
    }

    // ===============================
    // ✅ GET APPLICATION BY ID (🔥 REQUIRED FOR FEIGN)
    // ===============================
    @Hidden
    @GetMapping("/{id}")
    public LoanApplication getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ===============================
    // ✅ GET STATUS
    // ===============================
    @GetMapping("/{id}/status")
    public String getStatus(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {

        String token = header.substring(7);
        String role = jwtUtil.extractRole(token);

        if ("ADMIN".equalsIgnoreCase(role)) {
            return service.getStatus(id);
        }

        Long userId = getUserId(header);
        LoanApplication app = service.getById(id);

        if (!app.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You do not own this application");
        }

        return app.getStatus().name();
    }

    // ===============================
    // 🔥 ADMIN VIEW
    // ===============================
    @Hidden
    @GetMapping("/{id}/admin-view")
    public LoanApplication getForAdmin(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {

        validateAdmin(header);
        return service.getById(id);
    }

    // ===============================
    // 🔥 ADMIN GET ALL (INTERNAL ONLY)
    // ===============================
    @Hidden
    @GetMapping("/internal/all")
    public List<LoanApplication> getAll(
            @RequestHeader("Authorization") String header) {

        validateAdmin(header);
        return service.getAll(); // ✅ FIXED
    }

    // ===============================
    // 🔥 INTERNAL APPROVE
    // ===============================
    @Hidden
    @PostMapping("/{id}/internal/approve")
    public Map<String, String> approveInternal(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {
        validateAdmin(header);
        return service.approve(id);
    }

    // ===============================
    // 🔥 INTERNAL REJECT
    // ===============================
    @Hidden
    @PostMapping("/{id}/internal/reject")
    public Map<String, String> rejectInternal(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {
        validateAdmin(header);
        return service.reject(id);
    }
}