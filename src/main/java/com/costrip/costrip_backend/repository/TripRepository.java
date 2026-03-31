package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // 사용자의 전체 여행 목록 (최신순)
    List<Trip> findByUserOrderByCreatedAtDesc(User user);

    // 사용자의 상태별 여행 목록
    List<Trip> findByUserAndStatusOrderByCreatedAtDesc(User user, TripStatus status);

    // 사용자의 특정 여행 조회 (소유권 확인 포함)
    Optional<Trip> findByIdAndUser(Long id, User user);

    // 사용자의 여행 ID 존재 여부
    boolean existsByIdAndUser(Long id, User user);

    // 여행의 총 지출 금액 조회
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.trip.id = :tripId")
    java.math.BigDecimal sumExpenseAmountByTripId(@Param("tripId") Long tripId);
}
