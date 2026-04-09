package com.finflow.admin.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Report {

    @Id
    private Long id;

    private int totalApplications;
    private int approvedCount;
    private int rejectedCount;
    private int pendingCount;

    private LocalDateTime generatedAt;
}
