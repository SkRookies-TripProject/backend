package com.costrip.service;

import com.costrip.dto.statistics.BudgetSummaryResponseDto;
import com.costrip.dto.statistics.StatisticsResponseDto;
import com.costrip.dto.statistics.StatisticsResponseDto.DailyExpense;
import com.costrip.entity.Trip;
import com.costrip.entity.User;
import com.costrip.entity.enums.ExpenseCategory;
import com.costrip.repository.ExpenseRepository;
import com.costrip.repository.TripRepository;
import com.costrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * 카테고리별 소비 비율 + 일자별 지출 추이
     */
    public StatisticsResponseDto getStatistics(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);

        // ── 카테고리별 합계 ──────────────────────────────────────────────────
        List<Object[]> categoryRows = expenseRepository.sumAmountGroupByCategory(trip);

        Map<ExpenseCategory, BigDecimal> categoryAmounts = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Object[] row : categoryRows) {
            ExpenseCategory cat = (ExpenseCategory) row[0];
            BigDecimal amt     = (BigDecimal) row[1];
            categoryAmounts.put(cat, amt);
            grandTotal = grandTotal.add(amt);
        }

        // ── 카테고리별 비율 계산 ─────────────────────────────────────────────
        final BigDecimal total = grandTotal;
        Map<ExpenseCategory, Double> categoryRates = categoryAmounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> total.compareTo(BigDecimal.ZERO) == 0
                                ? 0.0
                                : e.getValue()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(total, 1, RoundingMode.HALF_UP)
                                    .doubleValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // ── 일자별 합계 ──────────────────────────────────────────────────────
        List<Object[]> dailyRows = expenseRepository.sumAmountGroupByDate(trip);

        Map<LocalDate, BigDecimal> dailyAmounts = new LinkedHashMap<>();
        for (Object[] row : dailyRows) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            dailyAmounts.put(date, amt);
        }

        // 차트용 정렬 리스트
        List<DailyExpense> dailyExpenses = dailyAmounts.entrySet().stream()
                .map(e -> DailyExpense.builder()
                        .date(e.getKey())
                        .amount(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return StatisticsResponseDto.builder()
                .categoryAmounts(categoryAmounts)
                .categoryRates(categoryRates)
                .dailyAmounts(dailyAmounts)
                .dailyExpenses(dailyExpenses)
                .build();
    }

    /**
     * 예산 사용 현황 (총 예산, 사용 금액, 잔여 예산, 사용률)
     */
    public BudgetSummaryResponseDto getBudgetSummary(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);

        BigDecimal totalBudget  = trip.getBudget() != null ? trip.getBudget() : BigDecimal.ZERO;
        BigDecimal totalSpent   = expenseRepository.sumAmountByTrip(trip);
        BigDecimal remaining    = totalBudget.subtract(totalSpent);

        double usageRate = totalBudget.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : totalSpent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalBudget, 1, RoundingMode.HALF_UP)
                    .doubleValue();

        return BudgetSummaryResponseDto.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .remainingBudget(remaining)
                .usageRate(usageRate)
                .build();
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Trip findTripByIdAndUser(Long tripId, User user) {
        return tripRepository.findByIdAndUser(tripId, user)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));
    }
}
