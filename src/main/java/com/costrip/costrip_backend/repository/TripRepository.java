package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    //  사용자 전체 여행 목록 (최신순)
    List<Trip> findByUserIdOrderByCreatedAtDesc(Long userId);

    //  사용자 특정 여행 조회 (소유권 체크)
    Optional<Trip> findByIdAndUserId(Long id, Long userId);

    //  사용자 여행 목록
    List<Trip> findByUserId(Long userId);

    Optional<Trip> findByIdAndUserEmail(Long id, String email);

    //  존재 여부 체크
    boolean existsByIdAndUserId(Long id, Long userId);

    //  여행 총 지출 금액
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip.id = :tripId")
    BigDecimal sumExpenseAmountByTripId(@Param("tripId") Long tripId);

    // 관리자 대시보드 (여행 순위 계산 용0
    @Query("""
    SELECT t.country, COUNT(t)
    FROM Trip t
    GROUP BY t.country
    ORDER BY COUNT(t) DESC
    """)
        List<Object[]> countGroupByCountry();
}
