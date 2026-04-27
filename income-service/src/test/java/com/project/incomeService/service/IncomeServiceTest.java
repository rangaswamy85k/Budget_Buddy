package com.project.incomeService.service;

import com.project.incomeService.feign.CategoryClient;
import com.project.incomeService.model.dto.CategoryDto;
import com.project.incomeService.model.dto.IncomeDto;
import com.project.incomeService.model.entity.Income;
import com.project.incomeService.repository.IncomeRepository;
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
public class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CategoryClient categoryClient;

    @InjectMocks
    private IncomeService incomeService;

    private static final Long USER_ID = 100L;
    private IncomeDto incomeDto;
    private Income incomeEntity;

    @BeforeEach
    void setUp() {
        incomeDto = IncomeDto.builder()
                .name("Salary")
                .amount(new BigDecimal("5000.00"))
                .categoryId(1L)
                .date(LocalDate.now())
                .build();

        incomeEntity = Income.builder()
                .id(1L)
                .name("Salary")
                .userId(USER_ID)
                .amount(new BigDecimal("5000.00"))
                .categoryId(1L)
                .date(LocalDate.now())
                .build();
    }

    @Test
    void createIncome_Success() {
        CategoryDto mockCategory = new CategoryDto();
        mockCategory.setId(1L);
        mockCategory.setName("Job");
        when(categoryClient.getCategoryById(1L)).thenReturn(ResponseEntity.ok(mockCategory));
        when(incomeRepository.save(any(Income.class))).thenReturn(incomeEntity);

        Income savedIncome = incomeService.createIncome(incomeDto, USER_ID);

        assertNotNull(savedIncome);
        assertEquals("Salary", savedIncome.getName());
        assertEquals(USER_ID, savedIncome.getUserId());
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    void createIncome_CategoryInvalid_ThrowsException() {
        when(categoryClient.getCategoryById(1L)).thenThrow(new RuntimeException("API Offline"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            incomeService.createIncome(incomeDto, USER_ID);
        });

        assertTrue(exception.getMessage().contains("Category service error"));
        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    void deleteIncome_Success() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(incomeEntity));
        doNothing().when(incomeRepository).delete(incomeEntity);

        assertDoesNotThrow(() -> incomeService.deleteIncome(1L, USER_ID));
        verify(incomeRepository, times(1)).delete(incomeEntity);
    }

    @Test
    void deleteIncome_Unauthorized_ThrowsException() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(incomeEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            incomeService.deleteIncome(1L, 999L); // Wrong user ID
        });

        assertEquals("Unauthorized to delete this income", exception.getMessage());
        verify(incomeRepository, never()).delete(any(Income.class));
    }

    @Test
    void getTotalIncome_ReturnsZeroIfNull() {
        when(incomeRepository.findTotalIncomeByUserId(USER_ID)).thenReturn(null);

        BigDecimal total = incomeService.getTotalIncome(USER_ID);

        assertEquals(BigDecimal.ZERO, total);
    }
}
