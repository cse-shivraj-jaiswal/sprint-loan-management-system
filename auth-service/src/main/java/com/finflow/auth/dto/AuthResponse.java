package com.finflow.auth.dto;

import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private Long userId;
    private String message;
}
