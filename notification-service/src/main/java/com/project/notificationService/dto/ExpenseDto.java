package com.project.notificationService.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseDto {
    private Long id;
    private String name;
    private Double amount;
    private LocalDate date;
    private Long categoryId;
    private Object category;
    private String categoryName; // Included as it relates to the user snippet logic
}
