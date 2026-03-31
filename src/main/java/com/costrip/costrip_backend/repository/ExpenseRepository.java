package com.costrip.costrip_backend.repository;

import com.costrip.entity.Expense;
import com.costrip.entity.Trip;
import com.costrip.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 여행의 전체 지출 목록 (최신순)
    List<Expense> findByTripOrderByExpenseDateDesc(Trip trip);

    // 여행 + 카테고리 필터
    List<Expense> findByTripAndCategoryOrderByExpenseDateDesc(Trip trip, ExpenseCategory category);

    // 여행 + 날짜 범위 필터
    List<Expense> findByTripAndExpenseDateBetweenOrderByExpenseDateDesc(
            Trip trip, LocalDate startDate, LocalDate endDate);

    // 여행 + 카테고리 + 날짜 범위 필터
    List<Expense> findByTripAndCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
            Trip trip, ExpenseCategory category, LocalDate startDate, LocalDate endDate);

    // 여행 + 금액 내림차순 정렬
    List<Expense> findByTripOrderByAmountDesc(Trip trip);

    // 특정 지출 조회 (소유권 확인: trip.user.email)
    @Query("SELECT e FROM Expense e JOIN e.trip t JOIN t.user u WHERE e.id = :expenseId AND u.email = :email")
    Optional<Expense> findByIdAndUserEmail(
            @Param("expenseId") Long expenseId,
            @Param("email") String email);

    // 여행의 총 지출 금액
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip = :trip")
    BigDecimal sumAmountByTrip(@Param("trip") Trip trip);

    // 카테고리별 합계 (통계용)
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.trip = :trip GROUP BY e.category")
    List<Object[]> sumAmountGroupByCategory(@Param("trip") Trip trip);

    // 일자별 합계 (통계용)
    @Query("SELECT e.expenseDate, SUM(e.amount) FROM Expense e WHERE e.trip = :trip GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<Object[]> sumAmountGroupByDate(@Param("trip") Trip trip);
}
