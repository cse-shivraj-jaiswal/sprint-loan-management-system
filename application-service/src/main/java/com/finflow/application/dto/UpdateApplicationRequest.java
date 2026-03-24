package com.finflow.application.dto;

import com.finflow.application.model.LoanType;
import com.finflow.application.model.OccupationType;
import lombok.Data;

@Data
public class UpdateApplicationRequest {

    private Double amount;
    private Integer tenure;
    private String employmentType;
    private Double income;

    private LoanType loanType;
    private OccupationType occupation;
    private Integer age;
    private String coApplicantName;
    private Double coApplicantIncome;
    private String coApplicantOccupation;
}