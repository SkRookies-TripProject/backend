package com.costrip.costrip_backend.dto.trip;

import com.costrip.costrip_backend.entity.Trip;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TripResponseDto {

    private Long id;
    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    // 🔥 예산 요약 (ExpenseBudget 기반)
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;

    public static TripResponseDto from(Trip trip) {
        return TripResponseDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .country(trip.getCountry())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .createdAt(trip.getCreatedAt())
                .build();
    }

    public static TripResponseDto from(
            Trip trip,
            BigDecimal totalBudget,
            BigDecimal totalSpent
    ) {
        BigDecimal remaining = (totalBudget != null && totalSpent != null)
                ? totalBudget.subtract(totalSpent)
                : null;

        return TripResponseDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .country(trip.getCountry())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .createdAt(trip.getCreatedAt())
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .remainingBudget(remaining)
                .build();
    }
}
