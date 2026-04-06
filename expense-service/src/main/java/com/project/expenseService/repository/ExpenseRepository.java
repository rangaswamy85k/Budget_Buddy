package com.project.expenseService.repository;

import com.project.expenseService.model.entity.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findTop5ByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    BigDecimal findTotalExpenseByUserId(Long userId);

    List<Expense> findByUserIdAndDateBetweenAndNameContainingIgnoreCase(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);
}
