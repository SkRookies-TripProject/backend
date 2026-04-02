package com.costrip.costrip_backend.dto.journal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JournalEntryRequestDto {

    @NotNull(message = "recordDate is required")
    private LocalDate recordDate;

    @Size(max = 1000, message = "memo must be 1000 characters or fewer")
    private String memo;
}
