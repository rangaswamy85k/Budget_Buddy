package com.project.dashboardservice.service;

import com.project.dashboardservice.client.ExpenseClient;
import com.project.dashboardservice.client.IncomeClient;
import com.project.dashboardservice.dto.ExpenseDto;
import com.project.dashboardservice.dto.IncomeDto;
import com.project.dashboardservice.dto.RecentTransactionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeClient incomeClient;
    private final ExpenseClient expenseClient;

    @CircuitBreaker(name = "dashboardService", fallbackMethod = "getDashboardDataFallback")
    public Map<String, Object> getDashboardData(String userIdStr) {
        Long userId = Long.parseLong(userIdStr);

        List<IncomeDto> latestIncomes = incomeClient.getLatestIncomes(userIdStr);
        List<ExpenseDto> latestExpenses = expenseClient.getLatestExpenses(userIdStr);

        BigDecimal totalIncome = incomeClient.getTotalIncome(userIdStr);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpense = expenseClient.getTotalExpense(userIdStr);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        List<RecentTransactionDTO> recentTransactions = Stream.concat(
                latestIncomes.stream().map(income ->
                        RecentTransactionDTO.builder()
                                .id(income.getId())
                                
                                .profileId(userId)
                                .icon(income.getCategory() != null ? income.getCategory().getIcon() : "")
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .type("income")
                                .build()),
                latestExpenses.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .profileId(userId)
                                .icon(expense.getCategory() != null ? expense.getCategory().getIcon() : "")
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .type("expense")
                                .build())
        ).sorted((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        }).collect(Collectors.toList());

        Map<String, Object> returnValue = new LinkedHashMap<>();
        returnValue.put("totalBalance", totalBalance);
        returnValue.put("totalIncome", totalIncome);
        returnValue.put("totalExpense", totalExpense);
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions);

        return returnValue;
    }

    public Map<String, Object> getDashboardDataFallback(String userIdStr, Throwable t) {
        Map<String, Object> fallbackData = new LinkedHashMap<>();
        fallbackData.put("totalBalance", BigDecimal.ZERO);
        fallbackData.put("totalIncome", BigDecimal.ZERO);
        fallbackData.put("totalExpense", BigDecimal.ZERO);
        fallbackData.put("recent5Expenses", List.of());
        fallbackData.put("recent5Incomes", List.of());
        fallbackData.put("recentTransactions", List.of());
        fallbackData.put("error", "One or more remote services are unavailable. Displaying default data: " + t.getMessage());
        return fallbackData;
    }
}
