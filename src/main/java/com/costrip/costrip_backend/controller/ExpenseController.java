package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.expense.ExpenseRequestDto;
import com.costrip.costrip_backend.dto.expense.ExpenseResponseDto;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * GET /api/trips/{tripId}/expenses
     * 지출 목록 조회 (카테고리·날짜 필터, 최신순·금액순 정렬)
     *
     * @param category   카테고리 필터 (선택)
     * @param startDate  조회 시작일 (선택)
     * @param endDate    조회 종료일 (선택)
     * @param sort       정렬 기준: "latest"(최신순, 기본) | "amount"(금액순)
     */
    @GetMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDto>>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "latest") String sort) {

        List<ExpenseResponseDto> expenses = expenseService.getExpenses(
                userDetails.getUsername(), tripId, category, startDate, endDate, sort);
        return ResponseEntity
                .ok(ApiResponse.success("지출 목록 조회 성공", expenses));
    }

    /**
     * POST /api/trips/{tripId}/expenses
     * 지출 등록
     */
    @PostMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> createExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId,
            @Valid @RequestBody ExpenseRequestDto requestDto) {

        ExpenseResponseDto responseDto = expenseService.createExpense(
                userDetails.getUsername(), tripId, requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("지출이 등록되었습니다.", responseDto));
    }

    /**
     * PUT /api/expenses/{expenseId}
     * 지출 수정
     */
    @PutMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> updateExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequestDto requestDto) {

        ExpenseResponseDto responseDto = expenseService.updateExpense(
                userDetails.getUsername(), expenseId, requestDto);
        return ResponseEntity
                .ok(ApiResponse.success("지출이 수정되었습니다.", responseDto));
    }

    /**
     * DELETE /api/expenses/{expenseId}
     * 지출 삭제
     */
    @DeleteMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long expenseId) {

        expenseService.deleteExpense(userDetails.getUsername(), expenseId);
        return ResponseEntity
                .ok(ApiResponse.success("지출이 삭제되었습니다.", null));
    }
}
