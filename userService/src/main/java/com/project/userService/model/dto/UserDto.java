package com.project.userService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserDto {

    private Long id;

    private String email;

    private String name;

    private Boolean isActive;

}
