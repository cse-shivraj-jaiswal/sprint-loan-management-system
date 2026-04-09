package com.finflow.application.repository;

import com.finflow.application.model.ApplicationStatus;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.LoanType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    // 🔥 ADD THIS
    List<LoanApplication> findByUserIdAndLoanType(Long userId, LoanType loanType);
}