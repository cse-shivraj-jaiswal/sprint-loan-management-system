package com.finflow.application.dto;

import com.finflow.application.model.LoanType;
import com.finflow.application.model.OccupationType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    @NotNull
    private Double amount;

    @NotNull
    private Integer tenure;

    private String employmentType;

    private Double income;

    @NotNull
    private LoanType loanType;
    
    private Integer age;
    private OccupationType occupation;

    private String coApplicantName;
    private Double coApplicantIncome;
    private String coApplicantOccupation;
}