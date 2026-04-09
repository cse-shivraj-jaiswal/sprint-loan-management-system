package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.model.Decision;
import com.finflow.admin.model.Report;
import com.finflow.admin.repository.DecisionRepository;
import com.finflow.admin.repository.ReportRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final ApplicationClient applicationClient;
    private final DocumentClient documentClient;
    private final DecisionRepository decisionRepo;
    private final ReportRepository reportRepo;

    public AdminService(ApplicationClient applicationClient,
                        DocumentClient documentClient,
                        DecisionRepository decisionRepo,
                        ReportRepository reportRepo) {
        this.applicationClient = applicationClient;
        this.documentClient = documentClient;
        this.decisionRepo = decisionRepo;
        this.reportRepo = reportRepo;
    }

    // ===============================
    // 🔍 VIEW APPLICATIONS
    // ===============================
    public List<Map<String, Object>> getApplications(String token) {

        List<Map<String, Object>> apps =
                applicationClient.getApplications(token);

        return apps.stream()
                .filter(app -> app != null && !"DRAFT".equalsIgnoreCase(String.valueOf(app.get("status"))))
                .toList();
    }

    // ===============================
    // 🔍 VIEW SINGLE APPLICATION (ADMIN VIEW)
    // ===============================
    public Map<String, Object> getApplicationById(Long appId, String token) {

        Map<String, Object> app = applicationClient.getApplicationById(token, appId);

        // 🚫 Block DRAFT applications from admin view
        String status = String.valueOf(app.get("status"));
        if ("DRAFT".equalsIgnoreCase(status)) {
            throw new RuntimeException("Application not found");
        }

        return app;
    }

    // ===============================
    // 📄 VIEW DOCUMENTS
    // ===============================
    public List<Map<String, Object>> getDocuments(Long appId, String token) {
        return (List<Map<String, Object>>) documentClient.getDocuments(appId, token);
    }

    // ===============================
    // ✅ VERIFY DOCUMENT
    // ===============================
    public Object verifyDocument(Long docId, String token) {
        return documentClient.verify(docId, token);
    }

    // ===============================
    // ❌ REJECT DOCUMENT
    // ===============================
    public Object rejectDocument(Long docId, String remarks, String token) {
        return documentClient.reject(docId, remarks, token);
    }



    // ✅ APPROVE APPLICATION
    // ===============================
    public Map<String, String> approve(Long appId, Long adminId, String token) {

        // 🔄 Check application status first
        Map<String, Object> app = applicationClient.getApplicationById(token, appId);
        String status = (String) app.get("status");

        if (!"SUBMITTED".equals(status)) {
            throw new RuntimeException("Application missing");
        }

        // 🔥 VALIDATION: Check if docs are verified
        List<Map<String, Object>> docs =
                (List<Map<String, Object>>) documentClient.getDocuments(appId, token);

        if (docs == null || docs.isEmpty()) {
            throw new RuntimeException("Firstly verify the documents.");
        }

        boolean allVerified = docs.stream()
                .allMatch(doc -> "VERIFIED".equals(doc.get("status")));

        if (!allVerified) {
            throw new RuntimeException("Firstly verify the documents.");
        }

        // 🔥 EXTRA CHECK: Check if all REQUIRED docs are present
        String loanType = (String) app.get("loanType");
        boolean isComplete = documentClient.validateDocuments(appId, loanType, token);
        if (!isComplete) {
            throw new RuntimeException("Firstly verify the documents.");
        }

        // 🔥 INTERNAL API CALL
        Map<String, String> response = applicationClient.approve(appId, token);

        // 💾 SAVE DECISION
        Decision decision = new Decision();
        decision.setApplicationId(appId);
        decision.setAdminId(adminId);
        decision.setDecision("APPROVED");
        decision.setCreatedAt(LocalDateTime.now());
        decisionRepo.save(decision);

        return response;
    }

    // ===============================
    // ❌ REJECT APPLICATION
    // ===============================
    public Map<String, String> reject(Long appId, Long adminId, String remarks, String token) {

        if (remarks == null || remarks.isBlank()) {
            throw new RuntimeException("Remarks are mandatory for rejection");
        }

        // 🔄 Check application status first
        Map<String, Object> app = applicationClient.getApplicationById(token, appId);
        String status = (String) app.get("status");

        if (!"SUBMITTED".equals(status)) {
            throw new RuntimeException("Application missing");
        }

        // 🔥 INTERNAL API CALL
        Map<String, String> response = applicationClient.reject(appId, token);

        // 💾 SAVE DECISION
        Decision decision = new Decision();
        decision.setApplicationId(appId);
        decision.setAdminId(adminId);
        decision.setDecision("REJECTED");
        decision.setRemarks(remarks);
        decision.setCreatedAt(LocalDateTime.now());
        decisionRepo.save(decision);

        return response;
    }

    // ===============================
    // 📊 DASHBOARD
    // ===============================
    public Report generateReport(Long adminId, String token) {

        List<Map<String, Object>> apps =
                applicationClient.getApplications(token);

        Report report = reportRepo.findById(adminId)
                .orElse(new Report());
        
        report.setId(adminId); // Ensure ID is from Auth Service (user table)

        if (apps != null) {

            List<Map<String, Object>> filtered = apps.stream()
                    .filter(a -> a != null && !"DRAFT".equalsIgnoreCase(String.valueOf(a.get("status"))))
                    .toList();

            report.setTotalApplications(filtered.size());

            long approved = filtered.stream()
                    .filter(a -> "APPROVED".equals(a.get("status")))
                    .count();

            long rejected = filtered.stream()
                    .filter(a -> "REJECTED".equals(a.get("status")))
                    .count();

            long pending = filtered.stream()
                    .filter(a -> "SUBMITTED".equalsIgnoreCase(String.valueOf(a.get("status"))))
                    .count();

            report.setApprovedCount((int) approved);
            report.setRejectedCount((int) rejected);
            report.setPendingCount((int) pending);
        }

        report.setGeneratedAt(LocalDateTime.now());

        return reportRepo.save(report);
    }
}