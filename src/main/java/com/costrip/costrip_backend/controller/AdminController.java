package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.auth.AdminDashboardResponseDto;
import com.costrip.costrip_backend.dto.auth.AdminStatsResponseDto;
import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     *  KPI 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDto>> getDashboard() {

        AdminDashboardResponseDto response = adminService.getDashboard();

        return ResponseEntity.ok(
                ApiResponse.success("관리자 대시보드 조회 성공", response)
        );
    }

    /**
     *  통계 조회 (Top5 + 카테고리 비율)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponseDto>> getStats() {

        AdminStatsResponseDto response = adminService.getStats();

        return ResponseEntity.ok(
                ApiResponse.success("관리자 통계 조회 성공", response)
        );
    }
}
