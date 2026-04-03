package com.costrip.costrip_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AdminDashboardResponseDto {

    private long totalUsers;
    private long totalTrips;
    private BigDecimal totalExpenseAmount;
}
