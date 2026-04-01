package com.costrip.costrip_backend.dto.expense;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ExpenseListResponseDto {

    private List<ExpenseResponseDto> expenses;
    private BigDecimal totalAmount;
}
