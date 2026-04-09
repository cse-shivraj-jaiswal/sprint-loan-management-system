package com.finflow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEvent {
    private String eventType;
    private String email;
    private String name;
    private String message;
    private String loanId;
    private String status;
}
