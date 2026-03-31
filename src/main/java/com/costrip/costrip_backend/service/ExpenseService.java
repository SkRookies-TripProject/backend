package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.expense.ExpenseRequestDto;
import com.costrip.costrip_backend.dto.expense.ExpenseResponseDto;

import java.util.List;

public interface ExpenseService {

    ExpenseResponseDto createExpense(String username, Long tripId, ExpenseRequestDto dto);

    List<ExpenseResponseDto> getExpenses(Long tripId);

    ExpenseResponseDto updateExpense(Long expenseId, ExpenseRequestDto dto);

    void deleteExpense(Long expenseId);
}