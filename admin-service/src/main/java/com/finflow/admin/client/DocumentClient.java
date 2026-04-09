package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @GetMapping("/documents/application/{appId}")
    Object getDocuments(
            @PathVariable Long appId,
            @RequestHeader("Authorization") String token
    );

    @PutMapping("/documents/internal/{docId}/verify")
    Object verify(
            @PathVariable Long docId,
            @RequestHeader("Authorization") String token
    );

    @PutMapping("/documents/internal/{docId}/reject")
    Object reject(
            @PathVariable Long docId,
            @RequestParam String remarks,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/documents/validate/{applicationId}/{loanType}")
    boolean validateDocuments(
            @PathVariable Long applicationId,
            @PathVariable String loanType,
            @RequestHeader("Authorization") String token
    );
}