package com.costrip.costrip_backend.dto.trip;

import com.costrip.costrip_backend.entity.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripRequestDto {

    @NotBlank(message = "여행 제목은 필수입니다.")
    @Size(max = 100, message = "여행 제목은 100자 이하이어야 합니다.")
    private String title;

    @NotBlank(message = "방문 국가는 필수입니다.")
    @Size(max = 100, message = "국가명은 100자 이하이어야 합니다.")
    private String country;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;
}
