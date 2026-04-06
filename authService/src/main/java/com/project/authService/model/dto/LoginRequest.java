package com.project.authService.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}

