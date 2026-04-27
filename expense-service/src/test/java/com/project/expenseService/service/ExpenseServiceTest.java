package com.project.expenseService.service;

import com.project.expenseService.feign.CategoryClient;
import com.project.expenseService.model.dto.CategoryDto;
import com.project.expenseService.model.dto.ExpenseDto;
import com.project.expenseService.model.entity.Expense;
import com.project.expenseService.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryClient categoryClient;

    @InjectMocks
    private ExpenseService expenseService;

    private static final Long USER_ID = 100L;
    private ExpenseDto expenseDto;
    private Expense expenseEntity;

    @BeforeEach
    void setUp() {
        expenseDto = ExpenseDto.builder()
                .name("Groceries")
                .amount(new BigDecimal("50.00"))
                .categoryId(5L)
                .date(LocalDate.now())
                .build();

        expenseEntity = Expense.builder()
                .id(1L)
                .name("Groceries")
                .userId(USER_ID)
                .amount(new BigDecimal("50.00"))
                .categoryId(5L)
                .date(LocalDate.now())
                .build();
    }

    @Test
    void createExpense_Success() {
        CategoryDto mockCategory = new CategoryDto();
        mockCategory.setId(5L);
        mockCategory.setName("Food");
        when(categoryClient.getCategoryById(5L)).thenReturn(ResponseEntity.ok(mockCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expenseEntity);

        Expense savedExpense = expenseService.createExpense(expenseDto, USER_ID);

        assertNotNull(savedExpense);
        assertEquals("Groceries", savedExpense.getName());
        assertEquals(USER_ID, savedExpense.getUserId());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createExpense_CategoryInvalid_ThrowsException() {
        when(categoryClient.getCategoryById(5L)).thenThrow(new RuntimeException("API Offline"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.createExpense(expenseDto, USER_ID);
        });

        assertTrue(exception.getMessage().contains("Category not found or invalid"));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_Success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expenseEntity));
        doNothing().when(expenseRepository).delete(expenseEntity);

        assertDoesNotThrow(() -> expenseService.deleteExpense(1L, USER_ID));
        verify(expenseRepository, times(1)).delete(expenseEntity);
    }

    @Test
    void deleteExpense_Unauthorized_ThrowsException() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expenseEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.deleteExpense(1L, 999L); // Wrong user ID
        });

        assertEquals("Unauthorized to delete this expense", exception.getMessage());
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    @Test
    void getTotalExpense_ReturnsZeroIfNull() {
        when(expenseRepository.findTotalExpenseByUserId(USER_ID)).thenReturn(null);

        BigDecimal total = expenseService.getTotalExpense(USER_ID);

        assertEquals(BigDecimal.ZERO, total);
    }
}
