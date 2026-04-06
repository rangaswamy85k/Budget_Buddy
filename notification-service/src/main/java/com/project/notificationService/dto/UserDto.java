package com.project.notificationService.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
}
