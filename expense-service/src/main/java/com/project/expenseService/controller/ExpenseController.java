package com.project.expenseService.controller;

import com.project.expenseService.model.dto.ExpenseDto;
import com.project.expenseService.model.entity.Expense;
import com.project.expenseService.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody ExpenseDto dto, @RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.createExpense(dto, userId));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getExpenses(userId));
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<ExpenseDto>> getCurrentMonthExpenses(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getCurrentMonthExpenses(userId));
    }

//    @DeleteMapping("/{expenseId}")
//    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId, @RequestHeader(value = "X-User-Id") String userIdStr) {
//        Long userId = Long.parseLong(userIdStr);
//        service.deleteExpense(expenseId, userId);
//        return ResponseEntity.noContent().build();
//    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(
            @PathVariable Long expenseId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr,
            @RequestParam(value = "userId", required = false) String paramUserId) {

        try {
            String actualUserIdStr = userIdStr != null ? userIdStr : paramUserId;
            if (actualUserIdStr == null || actualUserIdStr.isEmpty() || actualUserIdStr.equals("null")) {
                return ResponseEntity.badRequest().body("Missing User ID (Provide header X-User-Id or parameter userId)");
            }

            Long userId = Long.parseLong(actualUserIdStr);
            service.deleteExpense(expenseId, userId);
            return ResponseEntity.ok("Expense deleted successfully");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid User ID format");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/latest")
    public ResponseEntity<List<ExpenseDto>> getLatestExpenses(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getLatestExpenses(userId));
    }

    @GetMapping("/total")
    public ResponseEntity<java.math.BigDecimal> getTotalExpense(@RequestHeader(value = "X-User-Id") String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(service.getTotalExpense(userId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDto>> filterExpenses(
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
        return ResponseEntity.ok(service.filterExpenses(userId, start, end, keyword, sort));
    }
}
