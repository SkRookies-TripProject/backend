package com.costrip.costrip_backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class BudgetSummaryResponseDto {

    private BigDecimal totalBudget;     // 총 예산
    private BigDecimal totalSpent;      // 총 사용 금액
    private BigDecimal remainingBudget; // 잔여 예산
    private double usageRate;           // 예산 사용률 (%)
}
