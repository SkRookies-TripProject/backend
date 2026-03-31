package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.expense.ExpenseRequestDto;
import com.costrip.costrip_backend.dto.expense.ExpenseResponseDto;
import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 지출 목록 조회
     * - 카테고리·날짜 필터 및 최신순/금액순 정렬 지원
     */
    public List<ExpenseResponseDto> getExpenses(
            String email, Long tripId,
            ExpenseCategory category,
            LocalDate startDate, LocalDate endDate,
            String sort) {

        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);

        List<Expense> expenses;

        // 필터 조합에 따라 Repository 메서드 선택
        boolean hasCategory  = category != null;
        boolean hasDateRange = startDate != null && endDate != null;

        if (hasCategory && hasDateRange) {
            expenses = expenseRepository
                    .findByTripAndCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
                            trip, category, startDate, endDate);
        } else if (hasCategory) {
            expenses = expenseRepository
                    .findByTripAndCategoryOrderByExpenseDateDesc(trip, category);
        } else if (hasDateRange) {
            expenses = expenseRepository
                    .findByTripAndExpenseDateBetweenOrderByExpenseDateDesc(
                            trip, startDate, endDate);
        } else {
            expenses = expenseRepository.findByTripOrderByExpenseDateDesc(trip);
        }

        // 금액순 정렬 요청 시 재정렬
        if ("amount".equals(sort)) {
            expenses = expenseRepository.findByTripOrderByAmountDesc(trip);
        }

        return expenses.stream()
                .map(ExpenseResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 지출 등록
     * - 지출일이 여행 기간 내인지 검증
     */
    @Transactional
    public ExpenseResponseDto createExpense(String email, Long tripId, ExpenseRequestDto dto) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);
        validateExpenseDateInTripRange(dto.getExpenseDate(), trip);

        Expense expense = Expense.builder()
                .trip(trip)
                .expenseDate(dto.getExpenseDate())
                .category(dto.getCategory())
                .paymentMethod(dto.getPaymentMethod())
                .amount(dto.getAmount())
                .memo(dto.getMemo())
                .build();

        expenseRepository.save(expense);
        return ExpenseResponseDto.from(expense);
    }

    /**
     * 지출 수정
     * - 본인 여행의 지출인지 확인
     */
    @Transactional
    public ExpenseResponseDto updateExpense(String email, Long expenseId, ExpenseRequestDto dto) {
        Expense expense = findExpenseByIdAndUser(expenseId, email);
        validateExpenseDateInTripRange(dto.getExpenseDate(), expense.getTrip());

        expense.setExpenseDate(dto.getExpenseDate());
        expense.setCategory(dto.getCategory());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setAmount(dto.getAmount());
        expense.setMemo(dto.getMemo());

        return ExpenseResponseDto.from(expense);
    }

    /**
     * 지출 삭제
     * - 본인 여행의 지출인지 확인
     */
    @Transactional
    public void deleteExpense(String email, Long expenseId) {
        Expense expense = findExpenseByIdAndUser(expenseId, email);
        expenseRepository.delete(expense);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Trip findTripByIdAndUser(Long tripId, User user) {
        return tripRepository.findByIdAndUser(tripId, user)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private Expense findExpenseByIdAndUser(Long expenseId, String email) {
        return expenseRepository.findByIdAndUserEmail(expenseId, email)
                .orElseThrow(() -> new IllegalArgumentException("지출을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private void validateExpenseDateInTripRange(LocalDate expenseDate, Trip trip) {
        if (expenseDate == null) return;
        if (trip.getStartDate() != null && expenseDate.isBefore(trip.getStartDate())) {
            throw new IllegalArgumentException("지출일이 여행 시작일보다 이전입니다.");
        }
        if (trip.getEndDate() != null && expenseDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("지출일이 여행 종료일보다 이후입니다.");
        }
    }
}
