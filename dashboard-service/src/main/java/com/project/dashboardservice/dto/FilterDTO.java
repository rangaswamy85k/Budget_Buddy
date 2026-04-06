package com.project.dashboardservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FilterDTO {
    private String type; // "income" or "expense"
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // example: date, amount
    private String sortOrder; // example: asc, desc
}
