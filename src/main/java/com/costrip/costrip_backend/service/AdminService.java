package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.auth.AdminDashboardResponseDto;
import com.costrip.costrip_backend.dto.auth.AdminStatsResponseDto;
import com.costrip.costrip_backend.entity.enums.ExpenseCategory;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;

    /**
     *  KPI (기존)
     */
    public AdminDashboardResponseDto getDashboard() {

        long totalUsers = userRepository.count();
        long totalTrips = tripRepository.count();
        BigDecimal totalExpenseAmount = expenseRepository.getTotalAmount();

        return new AdminDashboardResponseDto(
                totalUsers,
                totalTrips,
                totalExpenseAmount
        );
    }

    /**
     *  통계 (Top5 + 카테고리 비율)
     */
    public AdminStatsResponseDto getStats() {

        // ================================
        // 1️⃣ Top5 여행지
        // ================================
        List<AdminStatsResponseDto.TopDestination> topDestinations =
                tripRepository.countGroupByCountry()
                        .stream()
                        .limit(5)
                        .map(r -> new AdminStatsResponseDto.TopDestination(
                                (String) r[0],   // country
                                (Long) r[1]      // count
                        ))
                        .toList();

        // ================================
        // 2️⃣ 카테고리 합계
        // ================================
        List<Object[]> results = expenseRepository.sumByCategory();

        BigDecimal total = results.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ================================
        // 3️⃣ 퍼센트 계산
        // ================================
        List<AdminStatsResponseDto.CategoryRatio> categoryRatios =
                results.stream()
                        .map(r -> {
                            ExpenseCategory category = (ExpenseCategory) r[0];
                            BigDecimal amount = (BigDecimal) r[1];

                            double percent = 0.0;

                            if (total.compareTo(BigDecimal.ZERO) > 0) {
                                percent = amount
                                        .divide(total, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .doubleValue();
                            }

                            return new AdminStatsResponseDto.CategoryRatio(
                                    category.name(), // enum → String
                                    percent
                            );
                        })
                        .toList();

        // ================================
        // 4️⃣ 반환
        // ================================
        return AdminStatsResponseDto.builder()
                .topDestinations(topDestinations)
                .categoryRatios(categoryRatios)
                .build();
    }
}
