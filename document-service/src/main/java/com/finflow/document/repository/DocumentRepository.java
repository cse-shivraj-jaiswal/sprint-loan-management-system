package com.finflow.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finflow.document.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // =========================
    // 📄 GET BY APPLICATION
    // =========================
    List<Document> findByApplicationId(Long applicationId);

    // =========================
    // 👤 GET BY USER
    // =========================
    List<Document> findByUserId(Long userId);

    // =========================
    // 🔍 FIND SPECIFIC DOCUMENT
    // =========================
    Optional<Document> findByApplicationIdAndDocumentType(
            Long applicationId,
            String documentType
    );

    // =========================
    // 🚫 CHECK DUPLICATE (IMPORTANT)
    // =========================
    boolean existsByApplicationIdAndDocumentType(
            Long applicationId,
            String documentType
    );
}