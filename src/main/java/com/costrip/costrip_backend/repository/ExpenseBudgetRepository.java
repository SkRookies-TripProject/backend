package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.ExpenseBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ExpenseBudgetRepository extends JpaRepository<ExpenseBudget, Long> {

    @Query("SELECT COALESCE(SUM(b.budgetAmount), 0) FROM ExpenseBudget b WHERE b.trip.id = :tripId")
    BigDecimal sumBudgetByTripId(@Param("tripId") Long tripId);
}