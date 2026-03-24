package com.finflow.application.service;

import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.OccupationType;

import org.springframework.stereotype.Service;

@Service
public class LoanValidationService {

    public void validate(LoanApplication app) {

        switch (app.getLoanType()) {

            case EDUCATION:
                if (app.getAge() < 16 || app.getAge() > 35) {
                    throw new BadRequestException("Invalid age for education loan");
                }
                if (app.getCoApplicantIncome() == null) {
                    throw new BadRequestException("Co-applicant required for education loan");
                }
                break;

            case HOME:
                if (app.getAge() < 21) {
                    throw new BadRequestException("Minimum age 21 required");
                }
                if (app.getIncome() == null || app.getIncome() < 20000) {
                    throw new BadRequestException("Insufficient income");
                }
                break;

            case BUSINESS:
            	if (app.getOccupation() != OccupationType.BUSINESS) {
            	    throw new BadRequestException("Only business owners allowed");
            	}
                break;

            case VEHICLE:
                if (app.getAge() < 18) {
                    throw new BadRequestException("Minimum age 18 required");
                }
                break;

            case MARRIAGE:
                if (app.getAge() < 21) {
                    throw new BadRequestException("Minimum age 21 required");
                }
                break;

            default:
                throw new BadRequestException("Invalid loan type");
        }
    }
}