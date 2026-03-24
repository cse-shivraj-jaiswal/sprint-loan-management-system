package com.finflow.application.service;

import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;
import com.finflow.application.exception.BadRequestException;
import com.finflow.application.exception.ResourceNotFoundException;
import com.finflow.application.model.ApplicationStatus;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository repository;
    private final LoanValidationService validationService;

    public LoanApplicationService(LoanApplicationRepository repository,
                                  LoanValidationService validationService) {
        this.repository = repository;
        this.validationService = validationService;
    }

    // 🔹 COMMON METHOD
    private LoanApplication getApplication(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    // ✅ CREATE APPLICATION (DRAFT)
    public LoanApplication create(Long userId, CreateApplicationRequest request) {

        LoanApplication app = new LoanApplication();

        app.setUserId(userId);
        app.setAmount(request.getAmount());
        app.setTenure(request.getTenure());
        app.setEmploymentType(request.getEmploymentType());
        app.setIncome(request.getIncome());

        // 🔥 ENUM FIELDS
        app.setLoanType(request.getLoanType());
        app.setOccupation(request.getOccupation());

        // 🔥 NEW FIELDS
        app.setAge(request.getAge());
        app.setCoApplicantName(request.getCoApplicantName());
        app.setCoApplicantIncome(request.getCoApplicantIncome());
        app.setCoApplicantOccupation(request.getCoApplicantOccupation());

        app.setStatus(ApplicationStatus.DRAFT);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        // 🔥 VALIDATION
        validationService.validate(app);

        return repository.save(app);
    }

    // ✅ UPDATE APPLICATION (ONLY DRAFT)
    public LoanApplication update(Long id, UpdateApplicationRequest request) {

        LoanApplication app = getApplication(id);

        // 🔥 RULE: Only DRAFT can be updated
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Only draft applications can be updated");
        }

        // 🔥 PARTIAL UPDATE
        if (request.getAmount() != null) {
            app.setAmount(request.getAmount());
        }

        if (request.getTenure() != null) {
            app.setTenure(request.getTenure());
        }

        if (request.getEmploymentType() != null) {
            app.setEmploymentType(request.getEmploymentType());
        }

        if (request.getIncome() != null) {
            app.setIncome(request.getIncome());
        }

        if (request.getLoanType() != null) {
            app.setLoanType(request.getLoanType());
        }

        if (request.getOccupation() != null) {
            app.setOccupation(request.getOccupation());
        }

        if (request.getAge() != null) {
            app.setAge(request.getAge());
        }

        if (request.getCoApplicantName() != null) {
            app.setCoApplicantName(request.getCoApplicantName());
        }

        if (request.getCoApplicantIncome() != null) {
            app.setCoApplicantIncome(request.getCoApplicantIncome());
        }

        if (request.getCoApplicantOccupation() != null) {
            app.setCoApplicantOccupation(request.getCoApplicantOccupation());
        }

        app.setUpdatedAt(LocalDateTime.now());

        return repository.save(app);
    }

    // ✅ SUBMIT APPLICATION (DRAFT → SUBMITTED)
    public LoanApplication submit(Long id) {

        LoanApplication app = getApplication(id);

        // 🔥 RULE: Only DRAFT → SUBMITTED
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Application already submitted");
        }

        // 🔥 VALIDATION BEFORE SUBMIT
        validationService.validate(app);

        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setUpdatedAt(LocalDateTime.now());

        return repository.save(app);
    }

    // ✅ GET USER APPLICATIONS
    public List<LoanApplication> getByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    // ✅ GET STATUS
    public String getStatus(Long id) {
        return getApplication(id).getStatus().name();
    }
}