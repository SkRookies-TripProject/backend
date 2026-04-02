package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.expense.ExpenseListResponseDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDto>>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "dateDesc") String sort) {

        List<ExpenseResponseDto> expenses = expenseService.getExpenses(
                userDetails.getUsername(),
                tripId,
                category,
                date,
                sort
        );

        return ResponseEntity
                .ok(ApiResponse.success("지출 목록 조회 성공", expenses));
    }

    @PostMapping("/trips/{tripId}/expenses")
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

    @GetMapping("/trips/{tripId}/expenses/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        BigDecimal totalAmount = expenseService.getTotalExpenseAmount(
                userDetails.getUsername(),
                tripId
        );

        return ResponseEntity
                .ok(ApiResponse.success("총 지출 금액 조회 성공", totalAmount));
    }

    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> updateExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequestDto requestDto) {

        ExpenseResponseDto responseDto = expenseService.updateExpense(
                userDetails.getUsername(), expenseId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("지출이 수정되었습니다.", responseDto));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long expenseId) {

        expenseService.deleteExpense(userDetails.getUsername(), expenseId);

        return ResponseEntity
                .ok(ApiResponse.success("지출이 삭제되었습니다.", null));
    }
}
