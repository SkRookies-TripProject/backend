package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.CardHistory;
import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardHistoryRepository extends JpaRepository<CardHistory, Long> {

    // 사용자의 카드내역 목록 (최신순)
    List<CardHistory> findByUserOrderByUsedAtDesc(User user);

    // 이미 연결된 지출인지 확인 (중복 연결 방지)
    boolean existsByLinkedExpense(Expense expense);

    // 지출에 연결된 카드내역 조회
    Optional<CardHistory> findByLinkedExpense(Expense expense);
}
