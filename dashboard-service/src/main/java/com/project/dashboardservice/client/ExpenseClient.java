package com.project.dashboardservice.client;

import com.project.dashboardservice.dto.ExpenseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "expense-service", url = "http://localhost:8084")
public interface ExpenseClient {

    @GetMapping("/expenses/latest")
    List<ExpenseDto> getLatestExpenses(@RequestHeader("X-User-Id") String userId);

    @GetMapping("/expenses/total")
    BigDecimal getTotalExpense(@RequestHeader("X-User-Id") String userId);

    @GetMapping("/expenses/filter")
    List<ExpenseDto> filterExpenses(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "sortField", required = false, defaultValue = "date") String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder
    );
}
