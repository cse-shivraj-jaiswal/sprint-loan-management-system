package com.finflow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private String eventType;
    private String email;
    private String name;
    private String message;
    private String documentType;
    private String loanId;
    private String applicationId;
}
