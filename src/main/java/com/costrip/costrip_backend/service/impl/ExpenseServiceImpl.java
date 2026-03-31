package com.costrip.costrip_backend.service.impl;

import com.costrip.costrip_backend.dto.expense.ExpenseRequestDto;
import com.costrip.costrip_backend.dto.expense.ExpenseResponseDto;
import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;

    @Override
    public ExpenseResponseDto createExpense(String username, Long tripId, ExpenseRequestDto dto) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        Expense expense = Expense.builder()
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .trip(trip)
                .build();

        return toDto(expenseRepository.save(expense));
    }

    @Override
    public List<ExpenseResponseDto> getExpenses(Long tripId) {
        return expenseRepository.findByTripId(tripId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ExpenseResponseDto updateExpense(Long expenseId, ExpenseRequestDto dto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());

        return toDto(expenseRepository.save(expense));
    }

    @Override
    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }

    private ExpenseResponseDto toDto(Expense e) {
        return ExpenseResponseDto.builder()
                .id(e.getId())
                .amount(e.getAmount())
                .category(e.getCategory())
                .description(e.getDescription())
                .build();
    }
}