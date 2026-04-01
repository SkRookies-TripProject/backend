package com.costrip.costrip_backend.dto.expense;

import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor

public class ExpenseBudgetRequestDto {

    @NotNull(message = "카테고리는 필수입니다.")
    private String category;

    @NotNull(message = "금액은 필수입니다.")
    private Long amount;
}
