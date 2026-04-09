package com.finflow.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.admin.model.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
