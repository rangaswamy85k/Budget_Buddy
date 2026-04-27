package com.project.dashboardservice.service;

import com.project.dashboardservice.client.ExpenseClient;
import com.project.dashboardservice.client.IncomeClient;
import com.project.dashboardservice.dto.ExpenseDto;
import com.project.dashboardservice.dto.IncomeDto;
import com.project.dashboardservice.dto.RecentTransactionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private IncomeClient incomeClient;

    @Mock
    private ExpenseClient expenseClient;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardData_CalculatesBalanceProperly() {
        String userIdStr = "100";

        when(incomeClient.getTotalIncome(userIdStr)).thenReturn(new BigDecimal("500.00"));
        when(expenseClient.getTotalExpense(userIdStr)).thenReturn(new BigDecimal("100.00"));
        
        when(incomeClient.getLatestIncomes(userIdStr)).thenReturn(List.of());
        when(expenseClient.getLatestExpenses(userIdStr)).thenReturn(List.of());

        Map<String, Object> data = dashboardService.getDashboardData(userIdStr);

        assertEquals(new BigDecimal("400.00"), data.get("totalBalance"));
        assertEquals(new BigDecimal("500.00"), data.get("totalIncome"));
        assertEquals(new BigDecimal("100.00"), data.get("totalExpense"));
    }

    @Test
    void getDashboardData_SortsRecentTransactionsAndCombinesThem() {
        String userIdStr = "100";
        
        IncomeDto income = new IncomeDto();
        income.setId(1L);
        income.setName("Salary");
        income.setAmount(new BigDecimal("1000.00"));
        income.setDate(LocalDate.of(2026, 4, 15));

        ExpenseDto expense = new ExpenseDto();
        expense.setId(2L);
        expense.setName("Groceries");
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.of(2026, 4, 18)); // Later date

        when(incomeClient.getTotalIncome(userIdStr)).thenReturn(BigDecimal.ZERO);
        when(expenseClient.getTotalExpense(userIdStr)).thenReturn(BigDecimal.ZERO);
        
        when(incomeClient.getLatestIncomes(userIdStr)).thenReturn(List.of(income));
        when(expenseClient.getLatestExpenses(userIdStr)).thenReturn(List.of(expense));

        Map<String, Object> data = dashboardService.getDashboardData(userIdStr);
        
        @SuppressWarnings("unchecked")
        List<RecentTransactionDTO> transactions = (List<RecentTransactionDTO>) data.get("recentTransactions");

        assertEquals(2, transactions.size());
        // Since expense was on April 18th, it should be sorted to the TOP (descending order)
        assertEquals("expense", transactions.get(0).getType());
        assertEquals("Groceries", transactions.get(0).getName());
        assertEquals("income", transactions.get(1).getType());
    }

    @Test
    void getDashboardDataFallback_ReturnsDefaultPojos() {
        Throwable dummyException = new RuntimeException("Expense Service Down");
        
        Map<String, Object> fallback = dashboardService.getDashboardDataFallback("100", dummyException);

        assertEquals(BigDecimal.ZERO, fallback.get("totalBalance"));
        assertEquals(BigDecimal.ZERO, fallback.get("totalIncome"));
        assertEquals(BigDecimal.ZERO, fallback.get("totalExpense"));
        
        @SuppressWarnings("unchecked")
        List<RecentTransactionDTO> transcations = (List<RecentTransactionDTO>) fallback.get("recentTransactions");
        assertTrue(transcations.isEmpty());

        assertEquals("One or more remote services are unavailable. Displaying default data: Expense Service Down", fallback.get("error"));
    }
}
