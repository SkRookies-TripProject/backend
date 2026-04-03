package com.costrip.costrip_backend.dto.journal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JournalEntryRequestDto {

    @NotNull(message = "기록 날짜는 필수입니다.")
    private LocalDate recordDate;

    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
    private String memo;
}
