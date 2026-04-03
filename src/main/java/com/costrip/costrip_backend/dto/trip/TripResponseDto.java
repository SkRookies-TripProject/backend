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
    private String thumbnailPath;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;

    /**
     * 여행 기본 정보와 썸네일만 응답으로 변환한다.
     */
    public static TripResponseDto from(Trip trip, String thumbnailPath) {
        return from(trip, null, null, thumbnailPath);
    }

    /**
     * 여행 정보와 예산 요약, 썸네일 경로를 함께 응답으로 변환한다.
     */
    public static TripResponseDto from(
            Trip trip,
            BigDecimal totalBudget,
            BigDecimal totalSpent,
            String thumbnailPath
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
                .thumbnailPath(thumbnailPath)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .remainingBudget(remaining)
                .build();
    }
}
