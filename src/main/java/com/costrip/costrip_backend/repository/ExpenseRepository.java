package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByTripIdOrderByExpenseDateDesc(Long tripId);

    List<Expense> findByTripIdAndCategoryOrderByExpenseDateDesc(Long tripId, ExpenseCategory category);

    List<Expense> findByTripIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long tripId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByTripIdAndCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long tripId, ExpenseCategory category, LocalDate startDate, LocalDate endDate);

    List<Expense> findByTripIdOrderByAmountDesc(Long tripId);

    @Query("SELECT e FROM Expense e JOIN e.trip t JOIN t.user u WHERE e.id = :expenseId AND u.email = :email")
    Optional<Expense> findByIdAndUserEmail(
            @Param("expenseId") Long expenseId,
            @Param("email") String email);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip.id = :tripId")
    BigDecimal sumAmountByTrip(@Param("tripId") Long tripId);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.trip.id = :tripId GROUP BY e.category")
    List<Object[]> sumAmountGroupByCategory(@Param("tripId") Long tripId);

    @Query("SELECT e.expenseDate, SUM(e.amount) FROM Expense e WHERE e.trip.id = :tripId GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<Object[]> sumAmountGroupByDate(@Param("tripId") Long tripId);
}
