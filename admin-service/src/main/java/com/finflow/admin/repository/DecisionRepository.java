package com.finflow.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.admin.model.Decision;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
}
