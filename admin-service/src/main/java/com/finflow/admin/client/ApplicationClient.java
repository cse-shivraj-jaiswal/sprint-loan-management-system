package com.finflow.admin.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "application-service")
public interface ApplicationClient {

    // ===============================
    // 🔍 GET ALL APPLICATIONS
    // ===============================
    @GetMapping("/applications/internal/all")
    List<Map<String, Object>> getApplications(
            @RequestHeader("Authorization") String token
    );

    // ===============================
    // 🔍 GET SINGLE APPLICATION (ADMIN VIEW)
    // ===============================
    @GetMapping("/applications/{id}/admin-view")
    Map<String, Object> getApplicationById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id
    );

    // ===============================
    // ✅ INTERNAL APPROVE (ONLY ADMIN-SERVICE USE)
    // ===============================
    @PostMapping("/applications/{id}/internal/approve")
    Map<String, String> approve(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    );

    // ===============================
    // ❌ INTERNAL REJECT (ONLY ADMIN-SERVICE USE)
    // ===============================
    @PostMapping("/applications/{id}/internal/reject")
    Map<String, String> reject(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    );
}