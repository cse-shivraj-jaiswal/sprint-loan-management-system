package com.finflow.application.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Double amount;
    private Integer tenure;
    private String employmentType;
    private Double income;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private OccupationType occupation;
    
    // Co-applicant
    private String coApplicantName;
    private Double coApplicantIncome;
    private String coApplicantOccupation;
}