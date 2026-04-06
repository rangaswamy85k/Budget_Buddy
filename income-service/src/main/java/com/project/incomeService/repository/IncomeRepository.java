package com.project.incomeService.repository;

import com.project.incomeService.model.entity.Income;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserId(Long userId);

    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> findTop5ByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId")
    BigDecimal findTotalIncomeByUserId(Long userId);

    List<Income> findByUserIdAndDateBetweenAndNameContainingIgnoreCase(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);
}
