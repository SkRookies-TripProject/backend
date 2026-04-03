package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.expense.ExpenseBudgetResponseDto;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseBudgetService {
    private final ExpenseBudgetRepository expenseBudgetRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 여행 전체 예산 합계 조회
     */
    public BigDecimal getTotalBudget(String email, Long tripId){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 본인 여행인지 확인 로직
        tripRepository.findByIdAndUserId(tripId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));

        return expenseBudgetRepository.sumBudgetByTripId(tripId);
    }

    /**
     * 특정 여행의 카테고리별 예산 목록 조회
     */
    public List<ExpenseBudgetResponseDto> getBudgetsByTrip(String email, Long tripId) {
        // 1. 본인 여행인지 확인 (이메일을 통해 직접 조회하는 메서드가 Repository에 있어야 함)
        Trip trip = tripRepository.findByIdAndUserEmail(tripId, email)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));

        // 2. 해당 여행에 연결된 모든 예산 항목 조회 및 DTO 변환
        return expenseBudgetRepository.findByTrip(trip)
                .stream()
                .map(ExpenseBudgetResponseDto::from)
                .collect(Collectors.toList());
    }
}