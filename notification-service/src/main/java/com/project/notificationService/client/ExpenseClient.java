package com.project.notificationService.client;

import com.project.notificationService.dto.ExpenseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "expense-service", url = "http://localhost:8084")
public interface ExpenseClient {

    @GetMapping("/expenses/filter")
    List<ExpenseDto> filterExpenses(
            @RequestHeader("X-User-Id") String userIdStr,
//            @RequestParam("startDate") LocalDate startDate,
//            @RequestParam("endDate") LocalDate endDate
            @RequestParam("startDate") String startDate,
           @RequestParam("endDate") String endDate
    );

}
