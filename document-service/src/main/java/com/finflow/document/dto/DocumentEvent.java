package com.finflow.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentEvent {
    private String eventType;
    private String userId;
    private String applicationId;
    private String documentType;
    private String status;
    private String remarks;
    private String email; // For notification purposes
    private String name;
    private String message;
}
