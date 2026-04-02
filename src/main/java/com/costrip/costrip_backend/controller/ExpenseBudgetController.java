package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.common.ApiResponse;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseBudgetController {

    private final ExpenseBudgetService expenseBudgetService;

    /**
     * 전체 예산 조회
     */
    @GetMapping("/trips/{tripId}/budget")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        BigDecimal totalBudget = expenseBudgetService.getTotalBudget(
                userDetails.getUsername(), tripId);

        return ResponseEntity
                .ok(ApiResponse.success("전체 예산 조회 성공", totalBudget));
    }
}
