package com.costrip.costrip_backend.dto.journal;

import com.costrip.costrip_backend.entity.journal.JournalEntry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class JournalEntryResponseDto {

    private Long entryId;
    private Long tripId;
    private LocalDate recordDate;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JournalEntryResponseDto from(JournalEntry journalEntry) {
        return JournalEntryResponseDto.builder()
                .entryId(journalEntry.getId())
                .tripId(journalEntry.getTrip().getId())
                .recordDate(journalEntry.getRecordDate())
                .memo(journalEntry.getMemo())
                .createdAt(journalEntry.getCreatedAt())
                .updatedAt(journalEntry.getUpdatedAt())
                .build();
    }
}
