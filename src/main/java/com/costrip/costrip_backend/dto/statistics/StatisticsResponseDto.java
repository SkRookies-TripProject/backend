package com.costrip.costrip_backend.dto.statistics;

import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsResponseDto {

    //전체 지출 합계
    private BigDecimal totalSpent;

    // 카테고리별 소비 비율 (카테고리 → 금액)
    private Map<ExpenseCategory, BigDecimal> categoryAmounts;

    // 카테고리별 비율 (카테고리 → %)
    private Map<ExpenseCategory, Double> categoryRates;

    // 일자별 지출 추이 (날짜 → 금액)
    private Map<LocalDate, BigDecimal> dailyAmounts;

    // 일자별 지출 리스트 (차트용 정렬된 데이터)
    private List<DailyExpense> dailyExpenses;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyExpense {
        private LocalDate date;
        private BigDecimal amount;
    }
}
