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

    // 🔹 특정 여행의 지출 전체 조회 (최신순)
    List<Expense> findByTripIdOrderByExpenseDateDesc(Long tripId);

    // 🔹 특정 여행 + 카테고리 필터 (최신순)
    List<Expense> findByTripIdAndCategoryOrderByExpenseDateDesc(
            Long tripId, ExpenseCategory category);

    // 🔹 특정 여행 + 날짜 단일 조회 (최신순)
    List<Expense> findByTripIdAndExpenseDateOrderByExpenseDateDesc(
            Long tripId, LocalDate expenseDate);

    // 🔹 특정 여행 + 카테고리 + 날짜 단일 조회 (최신순)
    List<Expense> findByTripIdAndCategoryAndExpenseDateOrderByExpenseDateDesc(
            Long tripId, ExpenseCategory category, LocalDate expenseDate);

    // 🔹 특정 여행의 지출 전체 조회 (금액 높은순)
    List<Expense> findByTripIdOrderByAmountDesc(Long tripId);

    // 🔹 지출 단건 조회 (보안: 본인 데이터만)
    @Query("SELECT e FROM Expense e JOIN e.trip t JOIN t.user u WHERE e.id = :expenseId AND u.email = :email")
    Optional<Expense> findByIdAndUserEmail(
            @Param("expenseId") Long expenseId,
            @Param("email") String email);

    // 🔹 특정 여행 총 지출 금액 (SUM)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip.id = :tripId")
    BigDecimal sumAmountByTrip(@Param("tripId") Long tripId);

    // 🔹 카테고리별 총 지출 금액
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.trip.id = :tripId GROUP BY e.category")
    List<Object[]> sumAmountGroupByCategory(@Param("tripId") Long tripId);

    // 🔹 날짜별 총 지출 금액
    @Query("SELECT e.expenseDate, SUM(e.amount) FROM Expense e WHERE e.trip.id = :tripId GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<Object[]> sumAmountGroupByDate(@Param("tripId") Long tripId);
}
