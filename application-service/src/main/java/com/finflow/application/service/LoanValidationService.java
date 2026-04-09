package com.finflow.application.service;

import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.OccupationType;
import org.springframework.stereotype.Service;

@Service
public class LoanValidationService {

    public void validate(LoanApplication app) {

        if (app.getLoanType() == null) {
            throw new BadRequestException("Loan type is required");
        }

        switch (app.getLoanType()) {

            case EDUCATION:
                validateEducationLoan(app);
                break;

            case HOME:
                validateHomeLoan(app);
                break;

            case BUSINESS:
                validateBusinessLoan(app);
                break;

            case VEHICLE:
                validateVehicleLoan(app);
                break;

            case MARRIAGE:
                validateMarriageLoan(app);
                break;

            case PERSONAL:
                validatePersonalLoan(app);
                break;

            default:
                throw new BadRequestException("Invalid loan type");
        }
    }

    // 🎓 EDUCATION
    private void validateEducationLoan(LoanApplication app) {

        if (app.getAge() < 16 || app.getAge() > 35) {
            throw new BadRequestException("Invalid age for education loan");
        }

        validateCoApplicant(app);
    }

    // 🏠 HOME
    private void validateHomeLoan(LoanApplication app) {

        if (app.getAge() < 21) {
            throw new BadRequestException("Minimum age 21 required");
        }

        if (app.getIncome() == null || app.getIncome() < 20000) {
            throw new BadRequestException("Insufficient income for home loan");
        }

        validateCoApplicant(app);
    }

    // 💼 BUSINESS
    private void validateBusinessLoan(LoanApplication app) {
    	
    	if (app.getAge() < 21) {
            throw new BadRequestException("Minimum age 21 required");
        }
        if (app.getOccupation() != OccupationType.BUSINESS) {
            throw new BadRequestException("Only business owners allowed");
        }

        if (app.getIncome() == null || app.getIncome() < 30000) {
            throw new BadRequestException("Minimum income 30000 required for business loan");
        }
        validateCoApplicant(app);
    }

    // 🚗 VEHICLE
    private void validateVehicleLoan(LoanApplication app) {

        if (app.getAge() < 21) {
            throw new BadRequestException("Minimum age 21 required");
        }

        if (app.getIncome() == null || app.getIncome() < 15000) {
            throw new BadRequestException("Minimum income 15000 required");
        }
        validateCoApplicant(app);
    }

    // 💍 MARRIAGE
    private void validateMarriageLoan(LoanApplication app) {

        if (app.getAge() < 21) {
            throw new BadRequestException("Minimum age 21 required");
        }

        if (app.getIncome() == null || app.getIncome() < 20000) {
            throw new BadRequestException("Minimum income 20000 required");
        }
        validateCoApplicant(app);
    }

    // 👤 PERSONAL
    private void validatePersonalLoan(LoanApplication app) {

        if (app.getAge() < 21) {
            throw new BadRequestException("Minimum age 21 required");
        }

        if (app.getIncome() == null || app.getIncome() < 15000) {
            throw new BadRequestException("Minimum income 15000 required");
        }
        validateCoApplicant(app);
    }

    // 🔥 COMMON CO-APPLICANT VALIDATION
    private void validateCoApplicant(LoanApplication app) {

        if (app.getCoApplicantName() == null || app.getCoApplicantName().trim().isEmpty()) {
            throw new BadRequestException("Co-applicant name is required");
        }

        if (app.getCoApplicantIncome() == null || app.getCoApplicantIncome() <= 0) {
            throw new BadRequestException("Valid co-applicant income required");
        }
    }
}