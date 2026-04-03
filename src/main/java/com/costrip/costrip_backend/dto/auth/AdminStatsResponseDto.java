package com.costrip.costrip_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminStatsResponseDto {

    private List<TopDestination> topDestinations;
    private List<CategoryRatio> categoryRatios;

    //  Top5 여행지
    @Getter
    @AllArgsConstructor
    public static class TopDestination {
        private String country;
        private Long count;
    }

    //  카테고리 비율
    @Getter
    @AllArgsConstructor
    public static class CategoryRatio {
        private String category;
        private double percent;
    }
}
