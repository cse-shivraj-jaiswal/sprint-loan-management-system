package com.finflow.application.service;

import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanValidationServiceTest {

    private LoanValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new LoanValidationService();
    }

    private LoanApplication validBaseApplication() {
        LoanApplication app = new LoanApplication();
        app.setAge(25);
        app.setIncome(50000.0);
        app.setCoApplicantName("Test User");
        app.setCoApplicantIncome(20000.0);
        return app;
    }

    // ✅ EDUCATION SUCCESS
    @Test
    void educationLoan_success() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.EDUCATION);

        assertDoesNotThrow(() -> validationService.validate(app));
    }

    // ❌ EDUCATION AGE FAIL
    @Test
    void educationLoan_invalid_age() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.EDUCATION);
        app.setAge(10);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }

    // ❌ HOME INCOME FAIL
    @Test
    void homeLoan_low_income() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.HOME);
        app.setIncome(10000.0);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }

    // ❌ BUSINESS WRONG OCCUPATION
    @Test
    void businessLoan_wrong_occupation() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.BUSINESS);
        app.setOccupation(OccupationType.SALARIED);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }

    // ❌ VEHICLE AGE FAIL
    @Test
    void vehicleLoan_invalid_age() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.VEHICLE);
        app.setAge(16);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }

    // ❌ CO-APPLICANT MISSING
    @Test
    void coApplicant_missing() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.HOME);
        app.setCoApplicantName(null);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }

    // ✅ PERSONAL SUCCESS
    @Test
    void personalLoan_success() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.PERSONAL);

        assertDoesNotThrow(() -> validationService.validate(app));
    }

    // ❌ PERSONAL INCOME FAIL
    @Test
    void personalLoan_low_income() {
        LoanApplication app = validBaseApplication();
        app.setLoanType(LoanType.PERSONAL);
        app.setIncome(10000.0);

        assertThrows(BadRequestException.class,
                () -> validationService.validate(app));
    }
}