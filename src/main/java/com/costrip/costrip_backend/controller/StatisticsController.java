package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.statistics.BudgetSummaryResponseDto;
import com.costrip.costrip_backend.dto.statistics.StatisticsResponseDto;
import com.costrip.costrip_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}")
@RequiredArgsConstructor
public class StatisticsController {

    private final UserService userService;

    /**
     * GET /api/trips/{tripId}/statistics
     * 카테고리별 비율 및 일자별 지출 추이
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<StatisticsResponseDto>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        StatisticsResponseDto responseDto = userService.getStatistics(
                userDetails.getUsername(), tripId);
        return ResponseEntity
                .ok(ApiResponse.success("통계 조회 성공", responseDto));
    }

    /**
     * GET /api/trips/{tripId}/budget-summary
     * 예산 사용 현황 (총 예산, 사용 금액, 잔여)
     */
    @GetMapping("/budget-summary")
    public ResponseEntity<ApiResponse<BudgetSummaryResponseDto>> getBudgetSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId) {

        BudgetSummaryResponseDto responseDto = userService.getBudgetSummary(
                userDetails.getUsername(), tripId);
        return ResponseEntity
                .ok(ApiResponse.success("예산 현황 조회 성공", responseDto));
    }
}
