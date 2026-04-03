package com.costrip.costrip_backend.dto.expense;

import com.costrip.costrip_backend.entity.ExpenseBudget;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExpenseBudgetResponseDto {
    private Long id;
    private String category;
    private BigDecimal amount;

    public static ExpenseBudgetResponseDto from(ExpenseBudget budget) {
        return ExpenseBudgetResponseDto.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .amount(BigDecimal.valueOf(budget.getAmount()))
                .build();
    }
}