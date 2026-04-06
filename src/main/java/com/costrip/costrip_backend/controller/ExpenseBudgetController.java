package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.expense.ExpenseBudgetResponseDto;
import com.costrip.costrip_backend.service.ExpenseBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class ExpenseBudgetController {

    private final ExpenseBudgetService expenseBudgetService;

    // 전체 예산
    @GetMapping("/{tripId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        BigDecimal totalBudget = expenseBudgetService.getTotalBudget(
                userDetails.getUsername(), tripId);

        return ResponseEntity
                .ok(ApiResponse.success("전체 예산 조회 성공", totalBudget));
    }

    // 카테고리별 예산 목록
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<List<ExpenseBudgetResponseDto>>> getBudgets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        List<ExpenseBudgetResponseDto> budgets =
                expenseBudgetService.getBudgetsByTrip(
                        userDetails.getUsername(), tripId);

        return ResponseEntity
                .ok(ApiResponse.success("예산 목록 조회 성공", budgets));
    }
}