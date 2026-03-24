package com.finflow.application.controller;

import com.finflow.application.model.LoanApplication;
import com.finflow.application.service.LoanApplicationService;
import org.springframework.web.bind.annotation.*;
import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    // ✅ CREATE
    @PostMapping
    public LoanApplication create(@RequestParam Long userId,
                                  @RequestBody CreateApplicationRequest request) {
        return service.create(userId, request);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public LoanApplication update(@PathVariable Long id,
                                  @RequestBody UpdateApplicationRequest request) {
        return service.update(id, request);
    }

    // ✅ SUBMIT
    @PostMapping("/{id}/submit")
    public LoanApplication submit(@PathVariable Long id) {
        return service.submit(id);
    }

    // ✅ GET BY USER (🔥 THIS IS MISSING)
    @GetMapping("/user/{userId}")
    public List<LoanApplication> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    // ✅ GET STATUS
    @GetMapping("/{id}/status")
    public String getStatus(@PathVariable Long id) {
        return service.getStatus(id);
    }
}