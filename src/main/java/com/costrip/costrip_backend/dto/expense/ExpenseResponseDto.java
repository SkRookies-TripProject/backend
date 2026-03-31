package com.costrip.costrip_backend.dto.expense;

import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseResponseDto {

    private Long id;
    private Long tripId;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseResponseDto from(Expense expense) {
        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .tripId(expense.getTrip().getId())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .paymentMethod(expense.getPaymentMethod())
                .amount(expense.getAmount())
                .memo(expense.getMemo())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
