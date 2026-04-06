package com.project.incomeService.controller;

import com.project.incomeService.model.dto.IncomeDto;
import com.project.incomeService.model.entity.Income;
import com.project.incomeService.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService service;

    @PostMapping
    public ResponseEntity<Income> createIncome(@RequestBody IncomeDto dto, @RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.createIncome(dto, userId));
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getIncomes(userId));
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<IncomeDto>> getCurrentMonthIncomes(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getCurrentMonthIncomes(userId));
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long incomeId, @RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        service.deleteIncome(incomeId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    public ResponseEntity<List<IncomeDto>> getLatestIncomes(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getLatestIncomes(userId));
    }

    @GetMapping("/total")
    public ResponseEntity<java.math.BigDecimal> getTotalIncome(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getTotalIncome(userId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<IncomeDto>> filterIncomes(
            @RequestHeader(value = "X-User-Id") String userIdStr,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "date") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        Long userId = Long.parseLong(userIdStr);
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);
        return ResponseEntity.ok(service.filterIncomes(userId, start, end, keyword, sort));
    }
}
