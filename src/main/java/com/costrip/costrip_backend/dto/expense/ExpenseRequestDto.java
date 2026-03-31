package com.costrip.costrip_backend.dto.expense;

import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ExpenseRequestDto {

    @NotNull(message = "지출 날짜는 필수입니다.")
    private LocalDate expenseDate;

    @NotNull(message = "카테고리는 필수입니다.")
    private ExpenseCategory category;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0원 초과이어야 합니다.")
    private BigDecimal amount;

    @Size(max = 500, message = "메모는 500자 이하이어야 합니다.")
    private String memo;
}
