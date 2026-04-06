package com.project.expenseService.service;

import com.project.expenseService.feign.CategoryClient;
import com.project.expenseService.model.dto.CategoryDto;
import com.project.expenseService.model.dto.ExpenseDto;
import com.project.expenseService.model.entity.Expense;
import com.project.expenseService.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final CategoryClient categoryClient;

    public Expense createExpense(ExpenseDto dto, Long userId) {
        // Validate Category (Temporarily commented for independent testing)
        // try {
        //     categoryClient.getCategoryById(dto.getCategoryId());
        // } catch (Exception e) {
        //      throw new RuntimeException("Category not found or invalid");
        // }

        Expense expense = Expense.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .categoryId(dto.getCategoryId())
                .userId(userId)
                .build();
        return repository.save(expense);
    }

    public List<ExpenseDto> getExpenses(Long userId) {
        return mapToDtoList(repository.findByUserId(userId));
    }

    public List<ExpenseDto> getCurrentMonthExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        return mapToDtoList(repository.findByUserIdAndDateBetween(userId, start, end));
    }

    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        repository.delete(expense);
    }

    public List<ExpenseDto> getLatestExpenses(Long userId) {
        return mapToDtoList(repository.findTop5ByUserIdOrderByDateDesc(userId));
    }

    public BigDecimal getTotalExpense(Long userId) {
        BigDecimal total = repository.findTotalExpenseByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<ExpenseDto> filterExpenses(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        List<Expense> expenses = repository.findByUserIdAndDateBetweenAndNameContainingIgnoreCase(userId, startDate, endDate, keyword, sort);
        return mapToDtoList(expenses);
    }

    private List<ExpenseDto> mapToDtoList(List<Expense> expenses) {
        return expenses.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ExpenseDto mapToDto(Expense expense) {
        CategoryDto category = null;
        try {
             category = categoryClient.getCategoryById(expense.getCategoryId()).getBody();
        } catch (Exception e) {
            // Log or ignore
        }
        return ExpenseDto.builder()
                .id(expense.getId())
                .name(expense.getName())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .categoryId(expense.getCategoryId())
                .category(category)
                .build();
    }
}
