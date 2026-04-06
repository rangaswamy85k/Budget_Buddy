package com.project.incomeService.service;

import com.project.incomeService.feign.CategoryClient;
import com.project.incomeService.model.dto.CategoryDto;
import com.project.incomeService.model.dto.IncomeDto;
import com.project.incomeService.model.entity.Income;
import com.project.incomeService.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository repository;
    private final CategoryClient categoryClient;

    public Income createIncome(IncomeDto dto, Long userId) {
        // Validate Category (Temporarily commented for independent testing, alike expense service)
        // try {
        //     categoryClient.getCategoryById(dto.getCategoryId());
        // } catch (Exception e) {
        //      throw new RuntimeException("Category not found or invalid");
        // }

        Income income = Income.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .categoryId(dto.getCategoryId())
                .userId(userId)
                .build();
        return repository.save(income);
    }

    public List<IncomeDto> getIncomes(Long userId) {
        return mapToDtoList(repository.findByUserId(userId));
    }

    public List<IncomeDto> getCurrentMonthIncomes(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        return mapToDtoList(repository.findByUserIdAndDateBetween(userId, start, end));
    }

    public void deleteIncome(Long incomeId, Long userId) {
        Income income = repository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        if (!income.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        repository.delete(income);
    }

    public List<IncomeDto> getLatestIncomes(Long userId) {
        return mapToDtoList(repository.findTop5ByUserIdOrderByDateDesc(userId));
    }

    public BigDecimal getTotalIncome(Long userId) {
        BigDecimal total = repository.findTotalIncomeByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<IncomeDto> filterIncomes(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        List<Income> incomes = repository.findByUserIdAndDateBetweenAndNameContainingIgnoreCase(userId, startDate, endDate, keyword, sort);
        return mapToDtoList(incomes);
    }

    private List<IncomeDto> mapToDtoList(List<Income> incomes) {
        return incomes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private IncomeDto mapToDto(Income income) {
        CategoryDto category = null;
        try {
             category = categoryClient.getCategoryById(income.getCategoryId()).getBody();
        } catch (Exception e) {
            // Log or ignore
        }
        return IncomeDto.builder()
                .id(income.getId())
                .name(income.getName())
                .amount(income.getAmount())
                .date(income.getDate())
                .categoryId(income.getCategoryId())
                .category(category)
                .build();
    }
}
