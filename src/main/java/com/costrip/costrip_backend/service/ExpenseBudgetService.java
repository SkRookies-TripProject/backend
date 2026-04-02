package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseBudgetService {
    private final ExpenseBudgetRepository expenseBudgetRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 여행 전체 예산 조회
     */
    public BigDecimal getTotalBudget(String email, Long tripId){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        tripRepository.findByIdAndUserId(tripId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));

        return expenseBudgetRepository.sumBudgetByTripId(tripId);
    }
}
