package com.project.dashboardservice.service;

import com.project.dashboardservice.client.ExpenseClient;
import com.project.dashboardservice.client.IncomeClient;
import com.project.dashboardservice.dto.ExpenseDto;
import com.project.dashboardservice.dto.FilterDTO;
import com.project.dashboardservice.dto.IncomeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterService {

    private final IncomeClient incomeClient;
    private final ExpenseClient expenseClient;

    public Object filterTransactions(FilterDTO filter, String userId) {
        String startDate = filter.getStartDate() != null ? filter.getStartDate().toString() : null;
        String endDate = filter.getEndDate() != null ? filter.getEndDate().toString() : null;
        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        String sortOrder = filter.getSortOrder() != null ? filter.getSortOrder() : "desc";

        if ("income".equalsIgnoreCase(filter.getType())) {
            List<IncomeDto> incomes = incomeClient.filterIncomes(userId, startDate, endDate, keyword, sortField, sortOrder);
            return incomes;
        } else if ("expense".equalsIgnoreCase(filter.getType())) {
            List<ExpenseDto> expenses = expenseClient.filterExpenses(userId, startDate, endDate, keyword, sortField, sortOrder);
            return expenses;
        } else {
            throw new IllegalArgumentException("Invalid type. Must be 'income' or 'expense'");
        }
    }
}
