package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.statistics.BudgetSummaryResponseDto;
import com.costrip.costrip_backend.dto.statistics.StatisticsResponseDto;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
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
    private final ExpenseBudgetRepository expenseBudgetRepository;

    public StatisticsResponseDto getStatistics(String email, Long tripId) {

        User user = findUserByEmail(email);
        findTripByIdAndUserId(tripId, user.getId());

        // 1. 카테고리별 합계
        List<Object[]> categoryRows = expenseRepository.sumAmountGroupByCategory(tripId);

        Map<ExpenseCategory, BigDecimal> categoryAmounts = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Object[] row : categoryRows) {
            ExpenseCategory cat = (ExpenseCategory) row[0];
            BigDecimal amt = (BigDecimal) row[1];

            categoryAmounts.put(cat, amt);
            grandTotal = grandTotal.add(amt);
        }

        // 카테고리 없는 항목도 0으로 채움
        for (ExpenseCategory category : ExpenseCategory.values()) {
            categoryAmounts.putIfAbsent(category, BigDecimal.ZERO);
        }

        // 2. 카테고리별 비율
        final BigDecimal total = grandTotal;

        Map<ExpenseCategory, Double> categoryRates =
                categoryAmounts.entrySet().stream()
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

        // 3. 일자별 합계
        List<Object[]> dailyRows = expenseRepository.sumAmountGroupByDate(tripId);

        Map<LocalDate, BigDecimal> dailyAmounts = new LinkedHashMap<>();

        for (Object[] row : dailyRows) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            dailyAmounts.put(date, amt);
        }

        List<StatisticsResponseDto.DailyExpense> dailyExpenses =
                dailyAmounts.entrySet().stream()
                        .map(e -> StatisticsResponseDto.DailyExpense.builder()
                                .date(e.getKey())
                                .amount(e.getValue())
                                .build())
                        .toList();

        // 4. 일자별 카테고리별 합계
        List<Object[]> dailyCategoryRows = expenseRepository.sumAmountGroupByDateAndCategory(tripId);

        Map<LocalDate, Map<ExpenseCategory, BigDecimal>> dailyCategoryAmounts = new LinkedHashMap<>();

        for (Object[] row : dailyCategoryRows) {
            LocalDate date = (LocalDate) row[0];
            ExpenseCategory category = (ExpenseCategory) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            dailyCategoryAmounts
                    .computeIfAbsent(date, d -> {
                        Map<ExpenseCategory, BigDecimal> categoryMap = new LinkedHashMap<>();
                        for (ExpenseCategory c : ExpenseCategory.values()) {
                            categoryMap.put(c, BigDecimal.ZERO);
                        }
                        return categoryMap;
                    })
                    .put(category, amount);
        }

        return StatisticsResponseDto.builder()
                .totalSpent(grandTotal)
                .categoryAmounts(categoryAmounts)
                .categoryRates(categoryRates)
                .dailyAmounts(dailyAmounts)
                .dailyCategoryAmounts(dailyCategoryAmounts)
                .dailyExpenses(dailyExpenses)
                .build();
    }

    public BudgetSummaryResponseDto getBudgetSummary(String email, Long tripId) {

        User user = findUserByEmail(email);
        findTripByIdAndUserId(tripId, user.getId());

        BigDecimal totalBudget = expenseBudgetRepository.sumBudgetByTripId(tripId);
        if (totalBudget == null) {
            totalBudget = BigDecimal.ZERO;
        }

        BigDecimal totalSpent = expenseRepository.sumAmountByTrip(tripId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        BigDecimal remaining = totalBudget.subtract(totalSpent);

        double usageRate = totalBudget.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(totalBudget, 1, RoundingMode.HALF_UP)
                .doubleValue();

        return BudgetSummaryResponseDto.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .remainingBudget(remaining)
                .usageRate(usageRate)
                .build();
    }
    // 헬퍼
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Trip findTripByIdAndUserId(Long tripId, Long userId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));
    }
}