package com.costrip.costrip_backend.dto.trip;

import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.enums.TripStatus;
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
    private BigDecimal budget;
    private TripStatus status;
    private LocalDateTime createdAt;

    // 예산 현황 요약 (목록 조회 시 편의 제공)
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;

    public static TripResponseDto from(Trip trip) {
        return TripResponseDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .country(trip.getCountry())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .status(trip.getStatus())
                .createdAt(trip.getCreatedAt())
                .build();
    }

    public static TripResponseDto from(Trip trip, BigDecimal totalSpent) {
        BigDecimal remaining = (trip.getBudget() != null && totalSpent != null)
                ? trip.getBudget().subtract(totalSpent)
                : null;

        return TripResponseDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .country(trip.getCountry())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .status(trip.getStatus())
                .createdAt(trip.getCreatedAt())
                .totalSpent(totalSpent)
                .remainingBudget(remaining)
                .build();
    }
}
